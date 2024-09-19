package com.example.exodia.calendar.service;

import com.example.exodia.calendar.dto.CalendarListDto;
import com.example.exodia.calendar.dto.CalendarSaveDto;
import com.example.exodia.calendar.repository.CalendarRepository;
import com.example.exodia.user.domain.User;
import com.example.exodia.user.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Service
public class CalendarService {

    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Value("${google.oauth.client-secret}")
    private String clientSecret;

    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth.credentials-file-path}")
    private String credentialsFilePath;

    @Value("${google.oauth.tokens-directory-path}")
    private String tokensDirectoryPath;

    /* CalendarService 인스턴스를 생성 */
    private Calendar getCalendarService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /* OAuth 2.0 인증 */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = CalendarService.class.getResourceAsStream(credentialsFilePath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /* 이벤트 생성(일정 생성) */
    public String createGoogleCalendarEvent(CalendarSaveDto dto) throws Exception {
        Calendar service = getCalendarService();

        // 이벤트 생성
        Event event = new Event()
                .setSummary(dto.getTitle())
                .setDescription(dto.getContent());

        // 시작시간
        DateTime startDateTime = new DateTime(dto.getStartTime().toString() + ":00Z");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        // 종료시간
        DateTime endDateTime = new DateTime(dto.getEndTime().toString() + ":00Z");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        // 이벤트 삽입
        event = service.events().insert("primary", event).execute();
        return event.getId();
    }

    /* 달 기준 이벤트 조회 */
    public List<Event> getGoogleCalendarEventsByMonth(CalendarListDto dto) throws Exception {
        com.google.api.services.calendar.Calendar service = getCalendarService();
        String monthStart = String.format("%04d-%02d-01T00:00:00Z", dto.getYear(), dto.getMonth());
        String monthEnd = String.format("%04d-%02d-%02dT23:59:59Z", dto.getYear(), dto.getMonth(), getLastDayOfMonth(dto.getYear(), dto.getMonth()));

        DateTime timeMin = new DateTime(monthStart);
        DateTime timeMax = new DateTime(monthEnd);

        Events events = service.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems();
    }

    private int getLastDayOfMonth(int year, int month) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
    }



    /*  이벤트 수정 */
    public String updateGoogleCalendarEvent(String eventId, CalendarSaveDto dto) throws Exception {
        Calendar service = getCalendarService();

        // 기존 이벤트 조회
        Event event = service.events().get("primary", eventId).execute();

        // 이벤트 정보 업데이트
        event.setSummary(dto.getTitle());
        event.setDescription(dto.getContent());

        DateTime startDateTime = new DateTime(dto.getStartTime().toString() + ":00Z");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        DateTime endDateTime = new DateTime(dto.getEndTime().toString() + ":00Z");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        event = service.events().update("primary", eventId, event).execute();
        return event.getId();
    }

    /*  이벤트 삭제(일정삭제) */
    public void deleteGoogleCalendarEvent(String eventId) throws Exception {
        Calendar service = getCalendarService();
        service.events().delete("primary", eventId).execute();
    }
}
