/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.onemrc.rpc.client.stubs;

import com.github.onemrc.rpc.client.RequestIdSupport;
import com.github.onemrc.rpc.client.ServiceStub;
import com.github.onemrc.rpc.client.ServiceTypes;
import com.github.onemrc.rpc.serialize.SerializeSupport;
import com.github.onemrc.rpc.transport.Transport;
import com.github.onemrc.rpc.transport.command.Code;
import com.github.onemrc.rpc.transport.command.Command;
import com.github.onemrc.rpc.transport.command.Header;
import com.github.onemrc.rpc.transport.command.ResponseHeader;

import java.util.concurrent.ExecutionException;


public abstract class AbstractStub implements ServiceStub {
    protected Transport transport;

    protected byte [] invokeRemote(RpcRequest request) {
        Header header = new Header(ServiceTypes.TYPE_RPC_REQUEST, 1, RequestIdSupport.next());
        byte [] payload = SerializeSupport.serialize(request);
        Command requestCommand = new Command(header, payload);
        try {
            Command responseCommand = transport.send(requestCommand).get();
            ResponseHeader responseHeader = (ResponseHeader) responseCommand.getHeader();
            if(responseHeader.getCode() == Code.SUCCESS.getCode()) {
                return responseCommand.getPayload();
            } else {
                throw new Exception(responseHeader.getError());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }
}
