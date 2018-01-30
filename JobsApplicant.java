package net.elenx.epomis.acceptor.pl.jobs;

import net.elenx.epomis.acceptor.applicant.ApplicationForm;
import net.elenx.epomis.acceptor.applicant.HtmlApplicant;
import net.elenx.epomis.acceptor.applicant.resume.UserResume;
import net.elenx.epomis.service.connection6.request.DataEntry;
import net.elenx.epomis.service.connection6.response.HtmlResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
class JobsApplicant implements HtmlApplicant
{
    private static final String OFFER_NUMBER_PATTERN = "[^0-9]";

    @Override
    public String urlFor(ApplicationForm<HtmlResponse> applicationForm)
    {
        return "https://www.jobs.pl/formularz-aplikacyjny-" + applicationForm.getJobOffer()
            .getHref()
            .replaceAll(OFFER_NUMBER_PATTERN, StringUtils.EMPTY);
    }

    @Override
    public boolean isAppropriateFor(ApplicationForm<?> applicationForm)
    {
        return applicationForm.getStatus() == ApplicationForm.Status.IN_PROGRESS
            && applicationForm.getCustomData().containsKey("JobsIsApplicationForm")
            && applicationForm.getCustomData().get("JobsIsApplicationForm").equals("JobsApplicationFormDownloaded");
    }

    @Override
    public Collection<DataEntry> constantDataEntries()
    {
        return Collections.singletonList(new DataEntry("rule_accept", "on"));
    }

    @Override
    public Collection<DataEntry> userDataEntries(UserResume userResume)
    {
        return Arrays.asList
            (
                new DataEntry("apply_msg", "Witam"),
                new DataEntry("user_file_1", StringUtils.EMPTY, userResume.getCv().getInputStream()),
                new DataEntry("user_file_1_type", "3"),
                new DataEntry("username", "login")
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
        return applicationForm
            .withStatus(currentResponse.isOk() ? ApplicationForm.Status.SUCCESS : ApplicationForm.Status.FAILURE)
            .withPreviousResponse(currentResponse);
    }
}
