## Product Name
Tracking Data Quality Backend Application

> the source code of the tdq backend service 

## Application Metadata

Application name: `tdqsvcng`

Cloud Console: `https://cloud.ebay.com/app/detail/tdqsvcng/overview`

## Building and Running

### Prerequisites
* eBay [OpenJDK 17](URL: 'go/jdk')
* MySQL 8
* IDE (Intellij IDEA recommended)

### Dependencies
`none`

### Build
```bash
mvn clean package -U -B -Pstatic-check-ng -Dstyle.color=always
```

### Run Parameters
`none`

## Contributing
`none`

## Resources

### Application Architecture
* [Raptor.io](URL: 'http://raptor.io.corp.ebay.com/')
* Spring Boot
* MyBatis Plus
* MySQL
* ElasticSearch

### Related products


### Documentation
- `http://raptor.io.corp.ebay.com/`

### Additional resources
- `https://docs.spring.io/spring-boot/docs/current/reference/html/`

## Support
- Jira project: `https://jirap.corp.ebay.com/browse/BHVRDT`
- Slack channel: `https://ebay.enterprise.slack.com/archives/C04QCQAHWD6`
- Email DL: `DL-eBay-Tracking-Data-Quality@ebay.com`