package com.example.digifix;

public class RepairModel {
    private String deviceName;
    private String customerInfo;
    private String priority; // "High", "Medium", "Normal"
    private String status; // "Pending", "In Progress", "Completed"
    private String date;
    private String assignee; // Initials
    private int iconResId;

    private String id;
    private String customerMobile;
    private String estimatedPrice;
    private String serialNumber;
    
    // Flutter Port Fields
    private boolean isCompleted;
    private long timestamp;
    private String addedBy;
    private String deviceType; // Maps to deviceName
    private String customDeviceName;
    
    // Restored Fields
    private String repairStage; // 'in_shop', 'at_center', 'completed'
    private String issue;
    private String accessories;
    private boolean isWarranty;
    private String centerName;
    private String warrantyOutcome;

    public RepairModel(String id, String deviceName, String customerInfo, String customerMobile, String estimatedPrice, String serialNumber, String priority, String status, String date, String assignee, int iconResId, String repairStage, String issue, String accessories, boolean isWarranty, String centerName, String warrantyOutcome) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceType = deviceName; // Default
        this.customerInfo = customerInfo;
        this.customerMobile = customerMobile;
        this.estimatedPrice = estimatedPrice;
        this.serialNumber = serialNumber;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.assignee = assignee;
        this.addedBy = assignee; // Default
        this.iconResId = iconResId;
        
        this.repairStage = repairStage;
        this.issue = issue;
        this.accessories = accessories;
        this.isWarranty = isWarranty;
        this.centerName = centerName;
        this.warrantyOutcome = warrantyOutcome;
        
        // Derive isCompleted
        this.isCompleted = "Completed".equalsIgnoreCase(status);
        this.timestamp = System.currentTimeMillis(); // Default to now
    }

    // New Constructor for simpler creation
    public RepairModel(String id, String customerName, String deviceType, String mobile, double price, boolean isCompleted) {
        this.id = id;
        this.customerInfo = customerName;
        this.deviceName = deviceType;
        this.deviceType = deviceType;
        this.customerMobile = mobile;
        this.estimatedPrice = String.valueOf(price);
        this.isCompleted = isCompleted;
        this.timestamp = System.currentTimeMillis();
        this.status = isCompleted ? "Completed" : "Pending";
    }

    public String getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public String getCustomerInfo() { return customerInfo; }
    public String getCustomerMobile() { return customerMobile; }
    public String getEstimatedPrice() { return estimatedPrice; }
    public String getSerialNumber() { return serialNumber; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getAssignee() { return assignee; }
    public int getIconResId() { return iconResId; }
    
    public String getRepairStage() { return repairStage; }
    public String getIssue() { return issue; }
    public String getAccessories() { return accessories; }
    public boolean isWarranty() { return isWarranty; }
    public String getCenterName() { return centerName; }
    public String getWarrantyOutcome() { return warrantyOutcome; }
    
    // New Getters
    public boolean isCompleted() { return isCompleted; }
    public long getTimestamp() { return timestamp; }
    public String getAddedBy() { return addedBy; }

    // Setters
    public void setStatus(String status) {
        this.status = status;
        this.isCompleted = "Completed".equalsIgnoreCase(status);
    }
    
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        this.status = completed ? "Completed" : "Pending";
    }
}
