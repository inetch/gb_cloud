package gb.cloud.client.network;

import gb.cloud.common.network.CommandMessage;

public interface IResponse {
    void gotOk(CommandMessage message);
    void gotError(CommandMessage message);
    void networkError();
}
