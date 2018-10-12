FROM library/openjdk:10-jre

WORKDIR /app

RUN apt-get update -qqy \
  && apt-get install -y fonts-liberation libappindicator3-1 libxss1 lsb-release xdg-utils \
  && wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
  && dpkg -i google-chrome-stable_current_amd64.deb \
  && rm google-chrome-stable_current_amd64.deb

RUN wget https://chromedriver.storage.googleapis.com/2.41/chromedriver_linux64.zip \
    && unzip chromedriver_linux64.zip \
    && rm chromedriver_linux64.zip \
    && chmod +x chromedriver

ARG LOGINSERVICE_VERSION=1.2
ADD ./target/LoginService-${LOGINSERVICE_VERSION}-jar-with-dependencies.jar /app/LoginService.jar
ADD ./run.sh /app/run.sh
RUN chmod +x /app/run.sh

EXPOSE 8080
CMD ["sh", "/app/run.sh"]
