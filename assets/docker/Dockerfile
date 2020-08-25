# Create runtime image
FROM nginx:mainline-alpine 
ENV WEB_DIR=/var/www/assets

RUN mkdir -p ${WEB_DIR}

COPY ${WORK_DIR}/assets ${WEB_DIR}/
COPY ${WORK_DIR}/docker/nginx.conf /etc/nginx/conf.d/default.conf