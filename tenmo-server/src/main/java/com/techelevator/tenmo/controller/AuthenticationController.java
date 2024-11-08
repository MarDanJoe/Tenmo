package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.model.*;
import jakarta.validation.Valid;

import com.techelevator.tenmo.exception.DaoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

/**
 * Controller to authenticate users.
 */
@RestController
public class AuthenticationController {
    private final TransferService transferService;

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserDao userDao;


    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao, TransferService transferService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.transferService = transferService;
    }

    @GetMapping("/transfers/pending")
    public List<TransferDTO> getPendingTransfers(Principal principal) {
        String username = principal.getName();
        List<TransferDTO> pendingTransfers = transferService.getPendingTransfersByUsername(username);

        for (TransferDTO transfer : pendingTransfers) {
            if (transfer.getTransferId() == null) {
                System.out.println("Warning: A transfer with a null ID was found.");

            }
        }

        return pendingTransfers;
    }
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public LoginResponseDto login(@Valid @RequestBody LoginDto loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);

        User user;
        try {
            user = userDao.getUserByUsername(loginDto.getUsername());
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
        }

        return new LoginResponseDto(jwt, user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void register(@Valid @RequestBody RegisterUserDto newUser) {
        try {
            if (userDao.getUserByUsername(newUser.getUsername()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists.");
            } else {
                userDao.createUser(newUser);
            }
        }
        catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User registration failed.");
        }
    }


    @GetMapping("/balance")
    public Balance getBalance(Principal principal) {

        String username = principal.getName();
        User user = userDao.getUserByUsername(username);

        return userDao.getBalanceByUserId(user.getId());
    }

@GetMapping("/transfers")
public List<TransferDTO> getTransferList(Principal principal) {
    if (principal == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }
    String username = principal.getName();
    return transferService.getTransfersForUser(username);
}


    @GetMapping("/transfers/{id}")
    public TransferDTO getTransfersById (Integer transferId) {
    TransferDTO transfer = transferService.getTransferById(transferId);
    if (transfer == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
    }
    return transfer;
    }
    @PostMapping("/transfers/send")
    public ResponseEntity<TransferDTO> sendBucks(@RequestBody TransferDTO transfer, Principal principal) {
        int authenticatedUserId = userDao.getUserByUsername(principal.getName()).getId();

        if (authenticatedUserId != transfer.getAccountFrom()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            TransferDTO createdTransfer = transferService.createTransfer(transfer);
            return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userDao.getUsers();
    }




}




