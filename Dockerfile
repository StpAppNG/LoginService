FROM library/openjdk:10-jre

WORKDIR /app

ARG LOGINSERVICE_VERSION=0.1

ARG CHROME_VERSION="google-chrome-stable"
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
  && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
  && apt-get update -qqy \
  && apt-get -qqy install \
    ${CHROME_VERSION:-google-chrome-stable} \
  && rm /etc/apt/sources.list.d/google-chrome.list \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

RUN wget https://chromedriver.storage.googleapis.com/2.41/chromedriver_linux64.zip \
    && unzip chromedriver_linux64.zip \
    && rm chromedriver_linux64.zip \
    && chmod +x chromedriver

ADD ./target/LoginService-${LOGINSERVICE_VERSION}-SNAPSHOT-jar-with-dependencies.jar /app/LoginService.jar
ADD ./run.sh /app/run.sh
RUN chmod +x /app/run.sh

EXPOSE 8080
CMD ["sh", "/app/run.sh"]
