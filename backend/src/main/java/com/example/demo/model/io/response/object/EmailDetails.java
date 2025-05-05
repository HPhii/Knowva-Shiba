package com.example.demo.model.io.response.object;

import com.example.demo.model.entity.Account;
import lombok.Data;

@Data
public class EmailDetails {
    Account receiver;
    String subject;
    String link;
}
