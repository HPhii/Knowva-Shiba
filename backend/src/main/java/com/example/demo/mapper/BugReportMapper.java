package com.example.demo.mapper;

import com.example.demo.model.entity.BugReport;
import com.example.demo.model.entity.BugReportReply;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.object.ReplyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BugReportMapper {

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.account.username", target = "reporterUsername")
    @Mapping(source = "replies", target = "replies")
    BugReportResponse toBugReportResponse(BugReport bugReport);

    List<BugReportResponse> toBugReportResponseList(List<BugReport> bugReports);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.account.username", target = "username")
    ReplyResponse toReplyResponse(BugReportReply reply);

    List<ReplyResponse> toReplyResponseList(List<BugReportReply> replies);
}