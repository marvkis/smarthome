/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.bosesoundtouch.handler;

import static org.eclipse.smarthome.binding.bosesoundtouch.BoseSoundTouchBindingConstants.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.APIRequest;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.BoseSoundTouchHandlerFactory;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.CommandExecutor;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.PresetContainer;
import org.eclipse.smarthome.binding.bosesoundtouch.internal.XMLResponseProcessor;
import org.eclipse.smarthome.binding.bosesoundtouch.types.OperationModeType;
import org.eclipse.smarthome.binding.bosesoundtouch.types.RemoteKeyType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoseSoundTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer - Initial contribution
 */
public class BoseSoundTouchHandler extends BaseThingHandler implements WebSocketListener {

    private final Logger logger = LoggerFactory.getLogger(BoseSoundTouchHandler.class);

    private ScheduledFuture<?> connectionChecker;
    private WebSocketClient client;
    private Session session;

    private XMLResponseProcessor xmlResponseProcessor;
    private BoseSoundTouchHandlerFactory factory;
    private CommandExecutor commandExecutor;

    private PresetContainer presetContainer;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     * @param factory the factory that created this handler
     *
     * @throws IllegalArgumentException if thing or factory argument is null
     */
    public BoseSoundTouchHandler(Thing thing, BoseSoundTouchHandlerFactory factory, PresetContainer presetContainer) {
        super(thing);
        if (factory == null) {
            throw new IllegalArgumentException("The argument 'factory' must not be null.");
        }
        this.factory = factory;
        this.presetContainer = presetContainer;
        xmlResponseProcessor = new XMLResponseProcessor(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoseSoundTouchHandler) {
            BoseSoundTouchHandler other = (BoseSoundTouchHandler) obj;
            if (this.getMacAddress().equals(other.getMacAddress())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return getMacAddress() + ": " + getDeviceName();
    }

    @Override
    public void initialize() {
        connectionChecker = scheduler.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                checkConnection();
            }
        }, 300, 300, TimeUnit.SECONDS);
        openConnection();
    }

    @Override
    public void dispose() {
        super.dispose();
        closeConnection();
        if (connectionChecker != null && !connectionChecker.isCancelled()) {
            connectionChecker.cancel(false);
        }
    }

    @Override
    public void handleRemoval() {
        factory.removeSoundTouchDevice(this);
        super.handleRemoval();
    }

    @Override // just overwrite to give CommandExecutor access
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}: handleCommand({}, {});", getDeviceName(), channelUID, command);
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (command.equals(RefreshType.REFRESH)) {
            switch (channelUID.getIdWithoutGroup()) {
                case CHANNEL_BASS:
                    commandExecutor.getInformations(APIRequest.BASS);
                    break;
                case CHANNEL_KEY_CODE:
                    // refresh makes no sense... ?
                    break;
                case CHANNEL_NOWPLAYING_ALBUM:
                case CHANNEL_NOWPLAYING_ARTIST:
                case CHANNEL_NOWPLAYING_ARTWORK:
                case CHANNEL_NOWPLAYING_DESCRIPTION:
                case CHANNEL_NOWPLAYING_GENRE:
                case CHANNEL_NOWPLAYING_ITEMNAME:
                case CHANNEL_NOWPLAYING_STATIONLOCATION:
                case CHANNEL_NOWPLAYING_STATIONNAME:
                case CHANNEL_NOWPLAYING_TRACK:
                case CHANNEL_RATEENABLED:
                case CHANNEL_SKIPENABLED:
                case CHANNEL_SKIPPREVIOUSENABLED:
                    commandExecutor.getInformations(APIRequest.NOW_PLAYING);
                    break;
                case CHANNEL_VOLUME:
                    commandExecutor.getInformations(APIRequest.VOLUME);
                    break;
                case CHANNEL_ZONE_ADD:
                case CHANNEL_ZONE_REMOVE:
                case CHANNEL_ZONE_INFO:
                    commandExecutor.getInformations(APIRequest.GET_ZONE);
                    break;
                default:
                    logger.warn("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                            channelUID.getId());
            }
            return;
        }
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    commandExecutor.postPower((OnOffType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_VOLUME:
                if (command instanceof PercentType) {
                    commandExecutor.postVolume((PercentType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    commandExecutor.postVolumeMuted((OnOffType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_OPERATIONMODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        command = OperationModeType.valueOf(cmd);
                    } catch (IllegalArgumentException iae) {
                        logger.warn("{}: OperationMode \"{}\" is not valid!", getDeviceName(), cmd);
                    }
                }
                if (command instanceof OperationModeType) {
                    commandExecutor.postOperationMode((OperationModeType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_PLAYER_CONTROL:
                if (command instanceof StringType) {
                    String cmd = command.toString();
                    if (cmd.equals("PLAY")) {
                        command = PlayPauseType.PLAY;
                    } else if (cmd.equals("PAUSE")) {
                        command = PlayPauseType.PAUSE;
                    } else if (cmd.equals("NEXT")) {
                        command = NextPreviousType.NEXT;
                    } else if (cmd.equals("PREVIOUS")) {
                        command = NextPreviousType.PREVIOUS;
                    }
                }
                if ((command instanceof PlayPauseType) || (command instanceof NextPreviousType)) {
                    commandExecutor.postPlayerControl(command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.postPreset((DecimalType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_BASS:
                if (command instanceof DecimalType) {
                    commandExecutor.postBass((DecimalType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_SAVE_AS_PRESET:
                if (command instanceof DecimalType) {
                    commandExecutor.addCurrentContentItemToPresetContainer((DecimalType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_KEY_CODE:
                if (command instanceof StringType) {
                    String cmd = command.toString().toUpperCase().trim();
                    try {
                        command = RemoteKeyType.valueOf(cmd);
                    } catch (IllegalArgumentException e) {
                        logger.warn("{}: Invalid remote key: {}", getDeviceName(), cmd);
                    }
                }
                if (command instanceof RemoteKeyType) {
                    commandExecutor.postRemoteKey((RemoteKeyType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_ZONE_ADD:
                if (command instanceof StringType) {
                    commandExecutor.postZoneAdd((StringType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            case CHANNEL_ZONE_REMOVE:
                if (command instanceof StringType) {
                    commandExecutor.postZoneRemove((StringType) command);
                } else {
                    logger.warn("{}: Invalid command type: {}: {}", getDeviceName(), command.getClass(), command);
                }
                break;
            default:
                logger.warn("{} : Got command '{}' for channel '{}' which is unhandled!", getDeviceName(), command,
                        channelUID.getId());
                break;
        }

    }

    /**
     * Returns the CommandExecutor of this handler
     *
     * @return the CommandExecutor of this handler
     */
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * Returns the BoseSoundTouchHandlerFactory this handler was created from
     *
     * @return the BoseSoundTouchHandlerFactory this handler was created from
     */
    public BoseSoundTouchHandlerFactory getFactory() {
        return factory;
    }

    /**
     * Returns the Session this handler has opened
     *
     * @return the Session this handler has opened
     */
    public Session getSession() {
        return session;
    }

    /**
     * Returns the name of the device delivered from itself
     *
     * @return the name of the device delivered from itself
     */
    public String getDeviceName() {
        return getThing().getProperties().get(DEVICE_INFO_NAME);
    }

    /**
     * Returns the type of the device delivered from itself
     *
     * @return the type of the device delivered from itself
     */
    public String getDeviceType() {
        return getThing().getProperties().get(DEVICE_INFO_TYPE);
    }

    /**
     * Returns the MAC Address of this device
     *
     * @return the MAC Address of this device
     */
    public String getMacAddress() {
        return (String) getThing().getConfiguration().get(Thing.PROPERTY_MAC_ADDRESS);
    }

    /**
     * Returns the IP Address of this device
     *
     * @return the IP Address of this device
     */
    public String getIPAddress() {
        return (String) getThing().getConfiguration().getProperties().get(DEVICE_PARAMETER_HOST);
    }

    /**
     * Returns the ChannelUID of a channelId String
     *
     * @param channelId the channelId is a String representing the channel
     *
     * @return the ChannelUID of a channelId String
     */
    public ChannelUID getChannelUID(String channelId) {
        Channel chann = getThing().getChannel(channelId);
        if (chann == null) {
            // refresh thing...
            Thing newThing = ThingFactory.createThing(thingTypeRegistry.getThingType(getThing().getThingTypeUID()),
                    getThing().getUID(), getThing().getConfiguration());
            updateThing(newThing);
            chann = getThing().getChannel(channelId);
        }
        return chann.getUID();
    }

    /**
     * Provides the handler internal scheduler instance
     *
     * @return the {@link ScheduledExecutorService} instance used by this handler
     */
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public PresetContainer getPresetContainer() {
        return this.presetContainer;
    }

    @Override
    public void updateThing(Thing thing) {
        super.updateThing(thing);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        logger.debug("{}: onWebSocketConnect('{}')", getDeviceName(), session);
        this.session = session;
        commandExecutor = new CommandExecutor(this);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onWebSocketError(Throwable e) {
        logger.error("{}: Error during websocket communication: {}", getDeviceName(), e.getMessage(), e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        commandExecutor.postOperationMode(OperationModeType.OFFLINE);
        if (session != null) {
            session.close(StatusCode.SERVER_ERROR, getDeviceName() + ": Failure: " + e.getMessage());
        }
    }

    @Override
    public void onWebSocketText(String msg) {
        logger.debug("{}: onWebSocketText('{}')", getDeviceName(), msg);
        try {
            xmlResponseProcessor.handleMessage(msg);
        } catch (Throwable s) {
            logger.error("{}: Could not parse XML from string '{}'; exception is: ", getDeviceName(), msg, s);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] arr, int pos, int len) {
        // we don't expect binary data so just dump if we get some...
        logger.debug("{}: onWebSocketBinary({}, {}, '{}')", pos, len, Arrays.toString(arr));
    }

    @Override
    public void onWebSocketClose(int code, String reason) {
        logger.debug("{}: onClose({}, '{}')", getDeviceName(), code, reason);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        commandExecutor.postOperationMode(OperationModeType.OFFLINE);
    }

    private void openConnection() {
        closeConnection();
        // updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE);
        try {
            client = new WebSocketClient();
            // we need longer timeouts for web socket.
            client.setMaxIdleTimeout(360 * 1000);
            // Port seems to be hard coded, therefore no user input or discovery is necessary
            String wsUrl = "ws://" + getIPAddress() + ":8080/";
            logger.debug("{}: Connecting to: {}", getDeviceName(), wsUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setSubProtocols("gabbo");
            client.start();
            client.connect(this, new URI(wsUrl), request);
        } catch (Exception e) {
            onWebSocketError(e);
        }
    }

    private void closeConnection() {
        if (session != null) {
            try {
                session.close(StatusCode.NORMAL, "Binding shutdown");
            } catch (Throwable e) {
                logger.error("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            session = null;
        }
        if (client != null) {
            try {
                client.stop();
                client.destroy();
            } catch (Exception e) {
                logger.error("{}: Error while closing websocket communication: {} ({})", getDeviceName(),
                        e.getClass().getName(), e.getMessage());
            }
            client = null;
        }
    }

    private void checkConnection() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            openConnection(); // try to reconnect....
        }
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                session.getRemote().sendString("HELLO");
            } catch (IOException e) {
                onWebSocketError(e);
                closeConnection();
                openConnection();
            }

        }
    }
}
