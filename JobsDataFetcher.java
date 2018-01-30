package net.elenx.epomis.acceptor.pl.jobs;

import com.google.api.client.http.HttpResponse;
import net.elenx.epomis.acceptor.applicant.ApplicationForm;
import net.elenx.epomis.acceptor.applicant.HtmlApplicant;
import net.elenx.epomis.service.connection6.ConnectionService6;
import net.elenx.epomis.service.connection6.request.ConnectionRequest;
import net.elenx.epomis.service.connection6.response.HtmlResponse;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
class JobsDataFetcher implements HtmlApplicant
{
    @Override
    public String urlFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return applicationForm.getJobOffer().getHref();
    }

    @Override
    public boolean isAppropriateFor(ApplicationForm<?> applicationForm)
    {
        System.out.println(applicationForm.getCustomData().toString());
        return applicationForm.getStatus() == ApplicationForm.Status.IN_PROGRESS
            && applicationForm.getCustomData().containsKey("JobsIsLogged")
            && applicationForm.getCustomData().get("JobsIsLogged").equals("JobsLogged");
    }

    @Override
    public Map<String, String> cookiesFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return Collections.singletonMap("sid", applicationForm.getCustomData().get("Sid"));
    }

    @Override
    public CompletableFuture<HtmlResponse> send(ConnectionService6 connectionService6, ConnectionRequest connectionRequest)
    {
        return connectionService6.getForHtml(connectionRequest);
    }

    @Override
    public ApplicationForm<HtmlResponse> advanceApplication(ApplicationForm<HtmlResponse> applicationForm, HtmlResponse currentResponse)
    {
        return applicationForm
            .withStatus(currentResponse.isOk() ? ApplicationForm.Status.IN_PROGRESS : ApplicationForm.Status.FAILURE)
            .withPreviousResponse(currentResponse)
            .withCustomData(crateCustomData(currentResponse, applicationForm));
    }

    Map<String, String> crateCustomData(HtmlResponse currentResponse, ApplicationForm applicationForm)
    {
        Document document = currentResponse.getDocument();

        Map<String, String> customData = new HashMap<>();
        customData.put("JobApplySendEmail", document.getElementsByClass("offer-apply-now").first().child(2).attributes().get("value"));
        customData.put("Sid", applicationForm.getCustomData().get("Sid").toString());

        return customData;
    }
}

