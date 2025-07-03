FROM openjdk:17
WORKDIR /app
COPY . .
RUN javac {{main_file}}
CMD ["java", "{{main_class}}"]
