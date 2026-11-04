# Iron Bank Compliance Process: Summary

### 1. **Base Image Selection**

- The first and most critical step is choosing an approved base image. Iron Bank provides pre-certified, digitally signed images that meet strict DoD requirements. Using these images streamlines the compliance process, as they are already validated and certified for security and compliance.   
  
<https://docs-ironbank.dso.mil/hardening/choosing-base-image/> 

### 2. **Container Hardening**

- All submitted containers must be hardened according to Iron Bank’s Acceptance Baseline Criteria (ABC). This involves:
    - Rebasing the container onto an Iron Bank image.
    - Ensuring the build process is internet-disconnected.
    - Supporting the application/container via a vendor, open source community, or government entity.
    - Using the latest release series for all dependencies and applications. [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/overview/)  

### 3. **Automated and Manual Scanning**

- Containers are scanned for vulnerabilities using tools like Anchore, OpenSCAP, and others. The process is heavily automated if approved components are used, but manual review is required for non-standard components or findings. [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/quickstart/components/)

### 4. **Continuous Monitoring**

- Iron Bank continuously monitors containers (currently every 12 hours) for new vulnerabilities. Any new findings must be addressed promptly, with justifications submitted for unresolved issues. [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/overview/)

### 5. **Findings Mitigation and Reporting**

- Vulnerabilities are managed according to the ABC, which sets strict timelines for remediation:
    - **Critical:** Within 5 days
    - **High:** Within 10 days
    - **Medium:** Within 30 days
    - **Low:** Within 60 days
- Justifications for unresolved findings must be documented and submitted through the Vulnerability Assessment Tracker (VAT). [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/reference/abc/)

### 6. **Submission and Review**

- Each container receives an Overall Risk Assessment (ORA) score, reflecting project health and open vulnerabilities.
- Updates to containers must be submitted on the day of public release.
- The Iron Bank team reviews findings, justifications, and compliance evidence before approving the container for inclusion. [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/overview/)

### 7. **Reciprocity and Deployment**

- Once approved, containers in Iron Bank have DoD-wide reciprocity, meaning they are trusted across all classifications and can be deployed rapidly, reducing the time and cost to obtain an Authority to Operate (ATO). [\[oteemo.com\]](https://oteemo.com/accelerate-to-iron-bank/)

---

## Key Takeaways

- **Strict adherence to approved base images and hardening guidelines is essential.**
- **Automated tools and manual review work together to ensure security.**
- **Continuous monitoring and rapid remediation of vulnerabilities are required.**
- **Documentation and justification of findings are critical for compliance.**
- **Iron Bank approval accelerates deployment and DoD-wide acceptance.**
