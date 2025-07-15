package com.example.demo.mapper;

import com.example.demo.model.entity.BugReport;
import com.example.demo.model.entity.BugReportReply;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.object.ReplyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BugReportMapper {

    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.account.username", target = "reporterUsername")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.account.username", target = "assigneeUsername")
    @Mapping(target = "replies", expression = "java(toTopLevelReplyResponses(bugReport.getReplies()))")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "priority", target = "priority")
    BugReportResponse toBugReportResponse(BugReport bugReport);

    List<BugReportResponse> toBugReportResponseList(List<BugReport> bugReports);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.account.username", target = "username")
    @Mapping(target = "children", expression = "java(toReplyResponseList(reply.getChildren()))")
    ReplyResponse toReplyResponse(BugReportReply reply);

    List<ReplyResponse> toReplyResponseList(List<BugReportReply> replies);

    default List<ReplyResponse> toTopLevelReplyResponses(List<BugReportReply> replies) {
        if (replies == null) {
            return java.util.Collections.emptyList();
        }
        return replies.stream()
                .filter(reply -> reply.getParent() == null)
                .map(this::toReplyResponse)
                .collect(Collectors.toList());
    }
}