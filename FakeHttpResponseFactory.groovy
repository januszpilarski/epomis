package net.elenx.epomis.connection.utils

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.LowLevelHttpRequest
import com.google.api.client.http.LowLevelHttpResponse
import com.google.api.client.testing.http.HttpTesting
import com.google.api.client.testing.http.MockHttpTransport
import com.google.api.client.testing.http.MockLowLevelHttpRequest
import com.google.api.client.testing.http.MockLowLevelHttpResponse

class FakeHttpResponseFactory {

    static HttpResponse createHttpResponse(int statusCodes, String message)
    {
        HttpTransport transport = new MockHttpTransport(){

            @Override
            LowLevelHttpRequest buildRequest(String method, String url) throws IOException
            {
                return new MockLowLevelHttpRequest(){
                    @Override
                    LowLevelHttpResponse execute() throws IOException
                    {
                        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse()
                        response.setStatusCode(statusCodes)
                        response.setReasonPhrase(message)

                        return response
                    }
                }
            }
        }

        HttpRequest request = transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL)
        request.setThrowExceptionOnExecuteError(false)
        return request.execute()
    }
}
