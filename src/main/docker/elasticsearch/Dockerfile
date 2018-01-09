FROM elasticsearch:5.6.5

RUN echo "script.inline: true" >> /usr/share/elasticsearch/config/elasticsearch.yml
RUN echo "transport.host: 0.0.0.0" >> /usr/share/elasticsearch/config/elasticsearch.yml
RUN echo "discovery.type: single-node" >> /usr/share/elasticsearch/config/elasticsearch.yml

CMD chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/data && gosu elasticsearch elasticsearch
