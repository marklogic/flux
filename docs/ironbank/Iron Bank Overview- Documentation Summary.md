# Iron Bank Overview: Documentation Summary

### What is Iron Bank?

Iron Bank is the Department of Defense’s (DoD) central repository for hardened container images. These containers are designed to run securely on authorized Kubernetes clusters that comply with the DevSecOps Reference Design. Iron Bank’s mission is to provide a secure-by-design baseline for containerised applications, enabling DoD organisations and commercial vendors to leverage pre-hardened, compliant images for their software deployments. [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/overview/)

### Key Features and Principles

- **Open Access:** Iron Bank and its associated repositories (Repo1, Registry1) are publicly accessible at Impact Level 2 (IL2). There are no restricted versions.
- **Hardened Containers:** All images must be hardened before being pushed to the registry. Direct uploads of external images are not supported.
- **Compliance and Security:** Iron Bank resolves many compliance findings and vulnerabilities through its processes. Containers are continuously monitored and scanned for security issues.
- **Manual and Automated Processes:** While much of the hardening process is automated, significant manual effort is involved in reviewing and validating containers.
- **No Built-in Licensing:** Iron Bank does not manage license keys or intellectual property protection; vendors must implement their own solutions.

### Who Uses Iron Bank?

- **DoD Organizations:** For consuming hardened containers and reviewing the Body of Evidence (BoE) for each image.
- **Authorization Officials:** To assess risks associated with applications.
- **Commercial Vendors:** To bring their applications to the DoD by meeting Iron Bank’s standards.
- **Contributors:** For bug fixes, new applications, and updates.

### Methodology and Compliance

- **Acceptance Baseline Criteria (ABC):** Containers are scanned and evaluated against Iron Bank’s ABC, receiving an Overall Risk Assessment (ORA) score.
- **Requirements:**
    - Containers must be rebased onto Iron Bank images.
    - Builds must be internet-disconnected.
    - Supported by a vendor, open source community, or government entity.
    - Use the latest release series for dependencies and applications.
    - Continuous monitoring and timely submission of justifications for new findings.
    - Updates must be submitted on the day of public release.

### Practical Guidance

- **Base Image Selection:** Use Iron Bank’s pre-certified images for compliance. The process is detailed and automated if approved components are used, but manual and time-consuming for non-approved components.
- **Security Standards:** Iron Bank follows strict guidelines, including the Security Technical Implementation Guide (STIG) and NIST-based requirements.
- **Impact Levels:** Images are classified as Unclassified (IL2), mapping to FedRamp levels. Higher classification levels are managed separately.

### Community Insights

- **Access:** Anyone can use Iron Bank; a DoD Common Access Card (CAC) is not required. Registration is open to the public.
- **CI/CD Integration:** Iron Bank images can be integrated into CI/CD pipelines. The key is ensuring artifacts are approved, scanned, and securely stored.
- **Scanning Tools:** Iron Bank uses tools like Prisma Cloud, Anchore, OpenSCAP, and Rapidfort for vulnerability assessment.

---

## References and Further Reading

- [Iron Bank Official Overview](https://docs-ironbank.dso.mil/overview/) [\[docs-ironb...nk.dso.mil\]](https://docs-ironbank.dso.mil/overview/)
- [Iron Bank Case Study](https://anchore.com/wp-content/uploads/2024/06/IRON-Bank-Case-Study_FINAL.pdf) [\[anchore.com\]](https://anchore.com/wp-content/uploads/2024/06/IRON-Bank-Case-Study_FINAL.pdf)
- [Community Experiences with Iron Bank](https://www.reddit.com/r/devops/comments/1bz0zjy/anyone_with_experience_using_iron_bank_base_images/) [\[reddit.com\]](https://www.reddit.com/r/devops/comments/1bz0zjy/anyone_with_experience_using_iron_bank_base_images/)

---

[Iron Bank Compliance Process: Summary](https://progresssoftware.atlassian.net/wiki/spaces/ENGINEERING/pages/2314076275/Iron+Bank+Compliance+Process+Summary)
