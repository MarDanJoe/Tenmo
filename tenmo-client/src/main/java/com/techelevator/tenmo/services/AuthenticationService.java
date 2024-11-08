package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.*;
import com.techelevator.util.BasicLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.List;

public class AuthenticationService {

    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthenticationService(String url) {
        this.baseUrl = url;
    }

    public AuthenticatedUser login(UserCredentials credentials) {
        HttpEntity<UserCredentials> entity = createCredentialsEntity(credentials);
        AuthenticatedUser user = null;
        try {
            ResponseEntity<AuthenticatedUser> response =
                    restTemplate.exchange(baseUrl + "login", HttpMethod.POST, entity, AuthenticatedUser.class);
            user = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }

    public boolean register(UserCredentials credentials) {
        HttpEntity<UserCredentials> entity = createCredentialsEntity(credentials);
        boolean success = false;
        try {
            restTemplate.exchange(baseUrl + "register", HttpMethod.POST, entity, Void.class);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    private HttpEntity<UserCredentials> createCredentialsEntity(UserCredentials credentials) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(credentials, headers);
    }


    public BigDecimal getBalance(AuthenticatedUser currentUser) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);


        ResponseEntity<Balance> response = restTemplate.exchange(
                baseUrl + "balance",
                HttpMethod.GET,
                entity,
                Balance.class
        );


        return response.getBody().getAmount();
    }





        public List<TransferDTO> getTransferHistory(AuthenticatedUser currentUser) {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(currentUser.getToken());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = baseUrl + "transfers";


            ResponseEntity<List<TransferDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<TransferDTO>>() {}
            );

            return response.getBody();
        }


    public List<TransferDTO> getPendingRequests(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = baseUrl + "transfers/pending";


        ResponseEntity<List<TransferDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<TransferDTO>>() {}
        );

        return response.getBody();
    }
    public TransferDTO sendBucks(AuthenticatedUser currentUser, TransferDTO transfer) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(currentUser.getToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TransferDTO> entity = new HttpEntity<>(transfer, headers);

            String url = baseUrl + "transfers/send";

            ResponseEntity<TransferDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TransferDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new Exception("This is from sendBUx "+ e.getMessage());
            //System.out.println("Error sending TE bucks: " + e.getMessage());
        } //return new TransferDTO();
    }

    public List<User> getAllUsers(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = baseUrl + "users";

        ResponseEntity<List<User>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<User>>() {}
        );

        return response.getBody();
    }
    public List<TransferType> getAllTransferTypes(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = baseUrl + "transfers/types";  // Ensure this endpoint is defined on your server

        ResponseEntity<List<TransferType>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<TransferType>>() {}
        );

        return response.getBody();  // Returns the list of transfer types
    }

    public List<TransferStatus> getAllTransferStatuses(AuthenticatedUser currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = baseUrl + "transfers/statuses";  // Ensure this endpoint is defined on your server

        ResponseEntity<List<TransferStatus>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<TransferStatus>>() {}
        );

        return response.getBody();  // Returns the list of transfer statuses
    }
}

