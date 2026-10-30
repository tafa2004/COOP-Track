package com.cooptrack;

import java.util.ArrayList;
import java.util.List;

public class Admin extends Member {

    private final List<Member> members = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private final List<Record> records = new ArrayList<>();
    private final List<String> invitationKeys = new ArrayList<>();

    public Admin() {
        super();
    }

    public Admin(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public boolean canManageTasks() {
        return true;
    }

    public void addMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null.");
        }
        members.add(member);
    }

    public List<Member> getMembers() {
        return List.copyOf(members);
    }

    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    public void addRecord(Record record) {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null.");
        }
        records.add(record);
    }

    public List<Record> getRecords() {
        return List.copyOf(records);
    }

    public void addInvitationKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Invitation key cannot be empty."
            );
        }
        invitationKeys.add(key);
    }

    public List<String> getInvitationKeys() {
        return List.copyOf(invitationKeys);
    }
}
