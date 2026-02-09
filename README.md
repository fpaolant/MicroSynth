# MicroSynth #

MicroSynth is a web based app written in Angular2+, Spring Boot, MongoDB for the automatic generation of synthetic microservice-based systems.
It allows users to model, generate, and export microservice systems starting from a high-level graphical representation.
Its goal is to provide a controlled environment for experimentation, benchmarking, and the study of distributed architectures, drastically reducing the time required to create complex systems.

| ![descrizione](https://github.com/fpaolant/MicroSynth/blob/main/client/src/public/img/microsynth_home_display_800.png) |
|:--:|

## Fast run

Follow these simple steps to run the project locally:

1. Clone repository:
   ```bash
   git clone https://github.com/fpaolant/MicroSynth.git

2. Access to project directory:
   ```bash
   cd MicroSynth

3. Run containers on docker:
   ```bash
   docker compose up --detach

4. Open browser with this address:
   ```bash
   http://localhost

5. SignIn with default admin credentials (change later)
    - u: admin
    - p: 12345
