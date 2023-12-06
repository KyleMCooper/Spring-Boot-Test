#!/bin/bash
# This script requires the credentials created by TestContainer at startup
# TODO: Adjust this script if is intended to be used on any other database than mysql
mysql -u test -ptest integration_tests < /opt/integration-tests-schema.sql