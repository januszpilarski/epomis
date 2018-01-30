package net.elenx.epomis.acceptor.pl.jobs;

import net.elenx.epomis.acceptor.applicant.ApplicationForm;
import net.elenx.epomis.acceptor.applicant.HtmlApplicant;
import net.elenx.epomis.service.connection6.request.DataEntry;
import net.elenx.epomis.service.connection6.response.HtmlResponse;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
class JobsFormDownloader implements HtmlApplicant
{
    private static final String OFFER_NUMBER_PATTERN = "[^0-9]";

    @Override
    public String urlFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return "https://www.jobs.pl/aplikuj-" + applicationForm.getJobOffer()
            .getHref()
            .replaceAll(OFFER_NUMBER_PATTERN, StringUtils.EMPTY);
    }

    @Override
    public boolean isAppropriateFor(ApplicationForm<?> applicationForm)
    {
        return applicationForm.getStatus() == ApplicationForm.Status.IN_PROGRESS
            && applicationForm.getCustomData().containsKey("JobApplySendEmail")
            && applicationForm.getCustomData().get("JobApplySendEmail") != null;
    }

    @Override
    public Collection<DataEntry> constantDataEntries()
    {
        return Collections.singletonList(new DataEntry("aplikuj", "Aplikuj teraz"));
    }

    @Override
    public Collection<DataEntry> previousResponseDataEntries(HtmlResponse previousResponse)
    {
        Document document = previousResponse.getDocument();

        return Arrays.asList
            (
                new DataEntry("jobApplySendEmail", document.getElementsByClass("offer-apply-now").first().child(2).val()),
                new DataEntry("offerLocId", document.getElementsByClass("offer-apply-now").first().child(0).val())
            );
    }

    @Override
    public Map<String, String> cookiesFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return Collections.singletonMap("sid", applicationForm.getCustomData().get("Sid"));
    }

    @Override
    public ApplicationForm<HtmlResponse> advanceApplication(ApplicationForm<HtmlResponse> applicationForm, HtmlResponse currentResponse)
    {
        boolean isSuccessful = currentResponse.isFound();

        return applicationForm
            .withStatus(isSuccessful ? ApplicationForm.Status.IN_PROGRESS : ApplicationForm.Status.FAILURE)
            .withPreviousResponse(currentResponse)
            .withCustomData(crateCustomData(applicationForm, isSuccessful));
    }

    Map<String, String> crateCustomData(ApplicationForm<HtmlResponse> applicationForm, boolean isSuccessful)
    {
        Map<String, String> customData = new HashMap<>();
        customData.put("JobsIsApplicationForm", isSuccessful ? "JobsApplicationFormDownloaded" : "JobsApplicationFormNotDownloaded");
        customData.put("Sid", applicationForm.getCustomData().get("Sid"));

        return customData;
    }
}