# Tracking Data Quality Backend Application

## Development

### Requirements
* eBay [OpenJDK 17](URL: 'go/jdk')
* MySQL 8
* IDE (Intellij IDEA recommended)

### Technology Stack
* [Raptor.io](URL: 'http://raptor.io.corp.ebay.com/')
* Spring Boot
* MyBatis Plus
* MySQL
* ElasticSearch

### Build
```bash
mvn clean package -U -B -Pstatic-check-ng -Dstyle.color=always
```
