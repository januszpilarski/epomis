package net.elenx.epomis.acceptor.pl.jobs;

import com.google.api.client.http.HttpResponse;
import net.elenx.epomis.acceptor.applicant.ApplicationForm;
import net.elenx.epomis.acceptor.applicant.HtmlApplicant;
import net.elenx.epomis.acceptor.applicant.resume.UserResume;
import net.elenx.epomis.service.connection6.request.DataEntry;
import net.elenx.epomis.service.connection6.response.HtmlResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
class JobsLogin implements HtmlApplicant
{
    @Override
    public String urlFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return "https://www.jobs.pl/logowanie";
    }

    @Override
    public boolean isAppropriateFor(ApplicationForm<?> applicationForm)
    {
        return applicationForm.getStatus() == ApplicationForm.Status.NEW
            && (Objects.isNull(applicationForm.getCustomData()) || applicationForm.getCustomData().isEmpty())
            && applicationForm.getJobOffer().getHref().startsWith("https://www.jobs.pl");
    }

    @Override
    public Collection<DataEntry> userDataEntries(UserResume userResume)
    {
        return Arrays.asList
            (
                new DataEntry("password", "haslo"),
                new DataEntry("username", "login")
            );
    }

    @Override
    public ApplicationForm<HtmlResponse> advanceApplication(ApplicationForm<HtmlResponse> applicationForm, HtmlResponse currentResponse)
    {
        boolean isSuccessful = currentResponse.isFound();

        return applicationForm
            .withStatus(isSuccessful ? ApplicationForm.Status.IN_PROGRESS : ApplicationForm.Status.FAILURE)
            .withPreviousResponse(currentResponse)
            .withCustomData(crateCustomData(currentResponse, isSuccessful));
    }

    Map<String, String> crateCustomData(HtmlResponse currentResponse, boolean isSuccessful)
    {
        Map<String, String> customData = new HashMap<>();
        customData.put("JobsIsLogged", isSuccessful ? "JobsLogged" : "JobsNotLogged");
        customData.put("Sid", currentResponse.getCookies().get("sid"));

        return customData;
    }
}

