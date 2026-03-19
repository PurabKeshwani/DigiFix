package com.example.digifix;

public class ClientModel {
    private String id;
    private String name;
    private String phone;
    private String email;
    private int activeJobsCount;
    private String initials;
    private boolean isOnline; // For status dot
    private String totalSpent; // e.g., "₹45k"
    private int deviceCount;
    private String membershipType; // "Premium", "Gold", "Standard"

    public ClientModel(String id, String name, String phone, String email, int activeJobsCount, String initials, boolean isOnline, String totalSpent, int deviceCount, String membershipType) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.activeJobsCount = activeJobsCount;
        this.initials = initials;
        this.isOnline = isOnline;
        this.totalSpent = totalSpent;
        this.deviceCount = deviceCount;
        this.membershipType = membershipType;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getActiveJobsCount() { return activeJobsCount; }
    public String getInitials() { return initials; }
    public boolean isOnline() { return isOnline; }
    public String getTotalSpent() { return totalSpent; }
    public int getDeviceCount() { return deviceCount; }
    public String getMembershipType() { return membershipType; }
}
