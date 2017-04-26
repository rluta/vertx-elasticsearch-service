FROM elasticsearch:2.4.4

RUN /usr/share/elasticsearch/bin/plugin install delete-by-query
RUN echo "script.inline: true" >> /usr/share/elasticsearch/config/elasticsearch.yml
RUN echo "script.indexed: true" >> /usr/share/elasticsearch/config/elasticsearch.yml

CMD chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/data && gosu elasticsearch elasticsearch
