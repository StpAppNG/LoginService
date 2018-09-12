FROM library/openjdk:10-jre

WORKDIR /app
# COPY config.json /app/config.json

RUN wget https://chromedriver.storage.googleapis.com/2.41/chromedriver_linux64.zip
RUN unzip chromedriver_linux64.zip
RUN rm chromedriver_linux64.zip
RUN chmod +x chromedriver

ARG CHROME_VERSION="google-chrome-stable"
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
  && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
  && apt-get update -qqy \
  && apt-get -qqy install \
    ${CHROME_VERSION:-google-chrome-stable} \
  && rm /etc/apt/sources.list.d/google-chrome.list \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

COPY target/LoginService-1.0-SNAPSHOT-jar-with-dependencies.jar /app/LoginService.jar
COPY ./run.sh /app/run.sh
RUN chmod +x /app/run.sh

EXPOSE 8080
CMD ["sh", "/app/run.sh"]
