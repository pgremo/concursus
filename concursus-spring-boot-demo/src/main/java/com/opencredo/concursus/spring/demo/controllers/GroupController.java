package com.opencredo.concursus.spring.demo.controllers;

import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.spring.demo.commands.GroupCommands;
import com.opencredo.concursus.spring.demo.services.GroupService;
import com.opencredo.concursus.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api/v1/acl/groups")
public class GroupController {

    private final GroupCommands groupCommands;
    private final GroupService groupService;

    @Autowired
    public GroupController(CommandProxyFactory commandBus, GroupService groupService) {
        groupCommands = commandBus.getProxy(GroupCommands.class);
        this.groupService = groupService;
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public Map<String, String> getGroups() {
        return groupService.getGroups();
    }


    @RequestMapping(path = "{groupId}", method = RequestMethod.GET)
    public GroupView getGroup(@PathVariable("groupId") UUID groupId) {
        return groupService.getGroup(groupId).orElseThrow(GroupNotFoundException::new);
    }

    @RequestMapping(path = "{groupId}", method = RequestMethod.DELETE)
    public CompletableFuture<ResponseEntity<?>> deleteGroup(@PathVariable("groupId") UUID groupId) {
        return groupCommands.delete(
                StreamTimestamp.of("admin", Instant.now()),
                groupId)
                .thenApply(v -> ResponseEntity.ok().build());
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> createGroup(@RequestBody String groupName) {
        UUID id  = UUID.randomUUID();
        return groupCommands.create(
                StreamTimestamp.of("admin", Instant.now()),
                id,
                groupName)
                .thenApply(groupId -> ResponseEntity.created(URI.create("/groups/" + groupId)).build());
    }

}
