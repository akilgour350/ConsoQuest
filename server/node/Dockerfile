FROM node:current-alpine

RUN mkdir node
COPY . ./node
WORKDIR ./node/

RUN npm install express pg

EXPOSE 18080

CMD node main.cjs