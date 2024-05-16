The ssl-server.json file does not declare any SSL config. Instead, it is added via the 
`TwoWaySslConfigurer` class. This is done via a separate app server so that if anything goes wrong 
with the setup or teardown, none of the other tests will be impacted.
