FROM node:current-alpine

RUN mkdir node
COPY . ./node
WORKDIR ./node/

RUN npm install express

EXPOSE 18080

CMD node app.js