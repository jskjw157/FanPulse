"""
API 앱 모델 - DDD 리팩토링 후 비워짐

모델이 다음 앱으로 이동됨:
- news: CrawledNews, CrawledChart, CrawledChartHistory, CrawledConcert, CrawledAd
- legacy: User, AuthToken, OAuthAccount, Artist, Poll 등 나머지 모델
- common: BaseModel (abstract)
"""
