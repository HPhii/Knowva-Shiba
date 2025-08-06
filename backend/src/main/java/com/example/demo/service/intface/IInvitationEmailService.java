package com.example.demo.service.intface;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Permission;

public interface IInvitationEmailService {
    void sendInvitationEmail(User inviter, User invitedUser, Long setId, String setName, String setType, Permission permission);
}