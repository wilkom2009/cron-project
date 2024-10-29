package com.wilkom.cronproject.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wilkom.cronproject.batch.BatchConfig;

@RestController
public class MainController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private BatchConfig batchConfig;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Step myStep1;

    @GetMapping
    public String hello() {
        InetAddress ip;
        String hostname = "";
        String hostip = "";
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            hostip = ip.toString();
        } catch (UnknownHostException e) {

            e.printStackTrace();
        }
        return "<html>Hello, Woezon, Bienvenido !!!! <br><br>" +
                "Sys date : <b>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:SS"))
                + "</b><br>" +
                "Your current IP address : " + hostip + "<br>" +
                "Your current Hostname : " + hostname + "<br>"
                + "</html>";
    }

    @GetMapping("/launch")
    public String launch() {
        JobParameters params = new JobParameters();
        try {
            jobLauncher.run(batchConfig.myJob(jobRepository, myStep1), params);
            return "OK";
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
        return "ERROR !";
    }
}
