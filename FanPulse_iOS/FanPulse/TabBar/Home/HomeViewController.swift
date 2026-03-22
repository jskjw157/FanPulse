//
//  HomeViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit
import SnapKit

final class HomeViewController: BaseViewController {
    
    // MARK: - UI Components
    
    private let scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsVerticalScrollIndicator = false
        scrollView.backgroundColor = UIColor(hex: "#F3F4F6")
        return scrollView
    }()
    
    private let contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    // MARK: - Header Banner
    
    private let headerBannerView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#8B5CF6")
        view.layer.cornerRadius = 16
        view.clipsToBounds = true
        return view
    }()
    
    private let bannerImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.image = UIImage(named: "img_banner")
        return imageView
    }()
    
    private let bannerTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "Welcome to FanPulse"
        label.font = .systemFont(ofSize: 24, weight: .bold)
        label.textColor = .white
        return label
    }()
    
    private let bannerSubtitleLabel: UILabel = {
        let label = UILabel()
        label.text = "글로벌 K-POP 팬들의 인터랙티브 플랫폼"
        label.font = .systemFont(ofSize: 13, weight: .regular)
        label.textColor = UIColor.white.withAlphaComponent(0.9)
        return label
    }()
    
    // MARK: - Latest News Section
    
    private let newsHeaderView = SectionHeaderView(
        icon: "newspaper.fill",
        title: "최신 뉴스",
        iconColor: UIColor(hex: "#8B5CF6")
    )
    
    private let newsStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        stackView.backgroundColor = .white
        stackView.layer.cornerRadius = 12
        stackView.clipsToBounds = true
        return stackView
    }()
    
    // MARK: - Live Now Section
    
    private let liveHeaderView = SectionHeaderView(
        icon: "video.fill",
        title: "Live Now",
        iconColor: UIColor(hex: "#EF4444")
    )
    
    private let liveScrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsHorizontalScrollIndicator = false
        return scrollView
    }()
    
    private let liveStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.spacing = 12
        return stackView
    }()
    
    // MARK: - Popular Content Section
    
    private let popularHeaderView = SectionHeaderView(
        icon: "flame.fill",
        title: "인기 게시글",
        iconColor: UIColor(hex: "#F59E0B")
    )
    
    private let popularStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 12
        return stackView
    }()
    
    // MARK: - Realtime Chart Section
    
    private let chartHeaderView = SectionHeaderView(
        icon: "chart.bar.fill",
        title: "실시간 차트",
        iconColor: UIColor(hex: "#8B5CF6")
    )
    
    private let chartStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        stackView.backgroundColor = .white
        stackView.layer.cornerRadius = 12
        stackView.clipsToBounds = true
        return stackView
    }()
    
    // MARK: - Voting Section
    
    private let votingTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "Best Male Group 2024"
        label.font = .systemFont(ofSize: 18, weight: .bold)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let voteNowButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Vote Now", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 13, weight: .semibold)
        button.setTitleColor(UIColor(hex: "#8B5CF6"), for: .normal)
        return button
    }()
    
    private let votingScrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsHorizontalScrollIndicator = false
        return scrollView
    }()
    
    private let votingStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.spacing = 12
        return stackView
    }()
    
    // MARK: - Upcoming Events Section
    
    private let eventsHeaderView = SectionHeaderView(
        icon: "calendar.badge.exclamationmark",
        title: "Upcoming Events",
        iconColor: UIColor(hex: "#EC4899")
    )
    
    private let eventsStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 12
        return stackView
    }()
    
    // MARK: - Bottom Action Buttons
    
    private let bottomActionsStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.spacing = 12
        stackView.distribution = .fillEqually
        return stackView
    }()
    
    // MARK: - Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = UIColor(hex: "#F3F4F6")
        configureNavigationBar(type: .home, true)
        setNavigationTitle()
        
        onSearchTapped = {
            print("검색")
        }
        
        onNotificationTapped = {
            let vc = NotificationViewController()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        
        setupUI()
        setupContent()
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        
        scrollView.snp.makeConstraints { make in
            make.edges.equalTo(view.safeAreaLayoutGuide)
        }
        
        contentView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
            make.width.equalToSuperview()
        }
        
        setupHeaderBanner()
        setupNewsSection()
        setupLiveSection()
        setupPopularSection()
        setupChartSection()
        setupVotingSection()
        setupEventsSection()
        setupBottomActions()
    }
    
    private func setupHeaderBanner() {
        contentView.addSubview(headerBannerView)
        headerBannerView.addSubview(bannerImageView)
        headerBannerView.addSubview(bannerTitleLabel)
        headerBannerView.addSubview(bannerSubtitleLabel)
        
        headerBannerView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(140)
        }
        
        bannerImageView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        bannerTitleLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(20)
            make.bottom.equalToSuperview().inset(36)
        }
        
        bannerSubtitleLabel.snp.makeConstraints { make in
            make.leading.equalTo(bannerTitleLabel)
            make.top.equalTo(bannerTitleLabel.snp.bottom).offset(4)
        }
    }
    
    private func setupNewsSection() {
        contentView.addSubview(newsHeaderView)
        contentView.addSubview(newsStackView)
        
        newsHeaderView.snp.makeConstraints { make in
            make.top.equalTo(headerBannerView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        newsStackView.snp.makeConstraints { make in
            make.top.equalTo(newsHeaderView.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
    }
    
    private func setupLiveSection() {
        contentView.addSubview(liveHeaderView)
        contentView.addSubview(liveScrollView)
        liveScrollView.addSubview(liveStackView)
        
        liveHeaderView.snp.makeConstraints { make in
            make.top.equalTo(newsStackView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        liveScrollView.snp.makeConstraints { make in
            make.top.equalTo(liveHeaderView.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(180)
        }
        
        liveStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16))
            make.height.equalToSuperview()
        }
    }
    
    private func setupPopularSection() {
        contentView.addSubview(popularHeaderView)
        contentView.addSubview(popularStackView)
        
        popularHeaderView.snp.makeConstraints { make in
            make.top.equalTo(liveScrollView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        popularStackView.snp.makeConstraints { make in
            make.top.equalTo(popularHeaderView.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
    }
    
    private func setupChartSection() {
        contentView.addSubview(chartHeaderView)
        contentView.addSubview(chartStackView)
        
        chartHeaderView.snp.makeConstraints { make in
            make.top.equalTo(popularStackView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        chartStackView.snp.makeConstraints { make in
            make.top.equalTo(chartHeaderView.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
    }
    
    private func setupVotingSection() {
        let headerContainer = UIView()
        headerContainer.addSubview(votingTitleLabel)
        headerContainer.addSubview(voteNowButton)
        
        contentView.addSubview(headerContainer)
        contentView.addSubview(votingScrollView)
        votingScrollView.addSubview(votingStackView)
        
        headerContainer.snp.makeConstraints { make in
            make.top.equalTo(chartStackView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        votingTitleLabel.snp.makeConstraints { make in
            make.leading.centerY.equalToSuperview()
        }
        
        voteNowButton.snp.makeConstraints { make in
            make.trailing.centerY.equalToSuperview()
        }
        
        votingScrollView.snp.makeConstraints { make in
            make.top.equalTo(headerContainer.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(220)
        }
        
        votingStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16))
            make.height.equalToSuperview()
        }
    }
    
    private func setupEventsSection() {
        contentView.addSubview(eventsHeaderView)
        contentView.addSubview(eventsStackView)
        
        eventsHeaderView.snp.makeConstraints { make in
            make.top.equalTo(votingScrollView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(40)
        }
        
        eventsStackView.snp.makeConstraints { make in
            make.top.equalTo(eventsHeaderView.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
    }
    
    private func setupBottomActions() {
        contentView.addSubview(bottomActionsStackView)
        
        bottomActionsStackView.snp.makeConstraints { make in
            make.top.equalTo(eventsStackView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(100)
            make.bottom.equalToSuperview().inset(24)
        }
    }
    
    private func setupContent() {
        setupNewsContent()
        setupLiveContent()
        setupPopularContent()
        setupChartContent()
        setupVotingContent()
        setupEventsContent()
        setupBottomActionsContent()
    }
    
    private func setupNewsContent() {
        let news = [
            ("BTS 새 앨범 발매 예정", "14시간 전"),
            ("BLACKPINK 월드투어 추가 공연", "3시간 전"),
            ("코첼라 BLACKPINK 필드투어 추가 공연", "3시간 전")
        ]
        
        for (index, item) in news.enumerated() {
            let newsItem = NewsItemView(title: item.0, time: item.1)
            newsStackView.addArrangedSubview(newsItem)
            
            if index < news.count - 1 {
                let separator = UIView()
                separator.backgroundColor = UIColor(hex: "#F3F4F6")
                newsStackView.addArrangedSubview(separator)
                separator.snp.makeConstraints { make in
                    make.height.equalTo(1)
                }
            }
        }
    }
    
    private func setupLiveContent() {
        let lives = [
            ("Music Bank Live", "125K", "img_live1"),
            ("Fan Meeting Live", "89K", "img_live2")
        ]
        
        for live in lives {
            let liveCard = LiveCardView(title: live.0, viewers: live.1, imageName: live.2)
            liveStackView.addArrangedSubview(liveCard)
            liveCard.snp.makeConstraints { make in
                make.width.equalTo(240)
            }
        }
    }
    
    private func setupPopularContent() {
        let posts = [
            ("BTS 콘서트 후기 - 정말 최고였어요!", "아이러버", 2645, 342, "img_post1"),
            ("NewJeans 신곡 뮤비 분석", "토끼덕후", 1923, 218, "img_post2"),
            ("아일릿 올려딛을 후 1티 예측", "K팝마니아", 1567, 189, "img_post3")
        ]
        
        for post in posts {
            let postCard = PopularPostCard(
                title: post.0,
                author: post.1,
                likes: post.2,
                comments: post.3,
                imageName: post.4
            )
            popularStackView.addArrangedSubview(postCard)
        }
    }
    
    private func setupChartContent() {
        let charts = [
            (1, "Super Shy", "NewJeans", "img_chart1", 0),
            (2, "Seven", "Jungkook (BTS)", "img_chart2", 1),
            (3, "Queencard", "(G)I-DLE", "img_chart3", -1),
            (4, "Spicy", "aespa", "img_chart4", 1),
            (5, "Kitsch", "IVE", "img_chart5", -1)
        ]
        
        for (index, chart) in charts.enumerated() {
            let chartItem = ChartItemView(
                rank: chart.0,
                title: chart.1,
                artist: chart.2,
                imageName: chart.3,
                change: chart.4
            )
            chartStackView.addArrangedSubview(chartItem)
            
            if index < charts.count - 1 {
                let separator = UIView()
                separator.backgroundColor = UIColor(hex: "#F3F4F6")
                chartStackView.addArrangedSubview(separator)
                separator.snp.makeConstraints { make in
                    make.height.equalTo(1)
                }
            }
        }
    }
    
    private func setupVotingContent() {
        let groups = [
            ("BTS", "2.5M", "1.2M", "img_bts"),
            ("BLACKPINK", "2.1M", "980K", "img_blackpink"),
            ("SEVENTEEN", "1.8M", "850K", "img_seventeen"),
            ("NewJeans", "1.5M", "720K", "img_newjeans")
        ]
        
        for group in groups {
            let voteCard = VoteCardView(
                groupName: group.0,
                followers: group.1,
                votes: group.2,
                imageName: group.3
            )
            votingStackView.addArrangedSubview(voteCard)
            voteCard.snp.makeConstraints { make in
                make.width.equalTo(150)
            }
        }
    }
    
    private func setupEventsContent() {
        let events = [
            ("Award Show", "2024.12.15", "MAMA Awards 2024", "img_event1"),
            ("Concert", "2024.12.20", "BTS World Tour Seoul", "img_event2")
        ]
        
        for event in events {
            let eventCard = EventCardView(
                type: event.0,
                date: event.1,
                title: event.2,
                imageName: event.3
            )
            eventsStackView.addArrangedSubview(eventCard)
        }
    }
    
    private func setupBottomActionsContent() {
        let actions = [
            ("gift.fill", "Earn Rewards", UIColor(hex: "#F59E0B")),
            ("crown.fill", "VIP Club", UIColor(hex: "#8B5CF6")),
            ("bubble.left.and.bubble.right.fill", "Community", UIColor(hex: "#3B82F6"))
        ]
        
        for action in actions {
            let actionButton = ActionButtonView(
                icon: action.0,
                title: action.1,
                color: action.2
            )
            bottomActionsStackView.addArrangedSubview(actionButton)
        }
    }
}

// MARK: - Section Header View

class SectionHeaderView: UIView {
    private let iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 18, weight: .bold)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let viewAllButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("View All", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 13, weight: .semibold)
        button.setTitleColor(UIColor(hex: "#8B5CF6"), for: .normal)
        return button
    }()
    
    init(icon: String, title: String, iconColor: UIColor) {
        super.init(frame: .zero)
        
        iconImageView.image = UIImage(systemName: icon)
        iconImageView.tintColor = iconColor
        titleLabel.text = title
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(iconImageView)
        addSubview(titleLabel)
        addSubview(viewAllButton)
        
        iconImageView.snp.makeConstraints { make in
            make.leading.equalToSuperview()
            make.centerY.equalToSuperview()
            make.width.height.equalTo(24)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(iconImageView.snp.trailing).offset(8)
            make.centerY.equalToSuperview()
        }
        
        viewAllButton.snp.makeConstraints { make in
            make.trailing.equalToSuperview()
            make.centerY.equalToSuperview()
        }
    }
}

// MARK: - News Item View

class NewsItemView: UIView {
    private let iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(systemName: "newspaper")
        imageView.tintColor = UIColor(hex: "#8B5CF6")
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .medium)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let timeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hex: "#9CA3AF")
        return label
    }()
    
    init(title: String, time: String) {
        super.init(frame: .zero)
        
        titleLabel.text = title
        timeLabel.text = time
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(iconImageView)
        addSubview(titleLabel)
        addSubview(timeLabel)
        
        iconImageView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(20)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(iconImageView.snp.trailing).offset(12)
            make.centerY.equalToSuperview()
            make.trailing.equalTo(timeLabel.snp.leading).offset(-8)
        }
        
        timeLabel.snp.makeConstraints { make in
            make.trailing.equalToSuperview().inset(16)
            make.centerY.equalToSuperview()
        }
        
        snp.makeConstraints { make in
            make.height.equalTo(56)
        }
    }
}

// MARK: - Live Card View

class LiveCardView: UIView {
    private let imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 12
        imageView.backgroundColor = UIColor(hex: "#8B5CF6")
        return imageView
    }()
    
    private let liveBadge: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#EF4444")
        view.layer.cornerRadius = 4
        return view
    }()
    
    private let liveLabel: UILabel = {
        let label = UILabel()
        label.text = "● LIVE"
        label.font = .systemFont(ofSize: 11, weight: .bold)
        label.textColor = .white
        return label
    }()
    
    private let viewersBadge: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        view.layer.cornerRadius = 10
        return view
    }()
    
    private let viewersLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 11, weight: .semibold)
        label.textColor = .white
        return label
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    init(title: String, viewers: String, imageName: String) {
        super.init(frame: .zero)
        
        titleLabel.text = title
        viewersLabel.text = "👁 \(viewers)"
        imageView.image = UIImage(named: imageName)
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(imageView)
        imageView.addSubview(liveBadge)
        liveBadge.addSubview(liveLabel)
        imageView.addSubview(viewersBadge)
        viewersBadge.addSubview(viewersLabel)
        addSubview(titleLabel)
        
        imageView.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.height.equalTo(140)
        }
        
        liveBadge.snp.makeConstraints { make in
            make.top.leading.equalToSuperview().offset(8)
            make.height.equalTo(24)
        }
        
        liveLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.horizontalEdges.equalToSuperview().inset(8)
        }
        
        viewersBadge.snp.makeConstraints { make in
            make.bottom.trailing.equalToSuperview().inset(8)
            make.height.equalTo(20)
        }
        
        viewersLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.horizontalEdges.equalToSuperview().inset(8)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom).offset(8)
            make.leading.trailing.bottom.equalToSuperview()
        }
    }
}

// MARK: - Popular Post Card

class PopularPostCard: UIView {
    private let thumbnailImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 8
        imageView.backgroundColor = UIColor(hex: "#E5E7EB")
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = UIColor(hex: "#111827")
        label.numberOfLines = 1
        return label
    }()
    
    private let authorLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hex: "#6B7280")
        return label
    }()
    
    private let statsLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hex: "#9CA3AF")
        return label
    }()
    
    init(title: String, author: String, likes: Int, comments: Int, imageName: String) {
        super.init(frame: .zero)
        
        titleLabel.text = title
        authorLabel.text = author
        statsLabel.text = "❤️ \(likes)  💬 \(comments)"
        thumbnailImageView.image = UIImage(named: imageName)
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(thumbnailImageView)
        addSubview(titleLabel)
        addSubview(authorLabel)
        addSubview(statsLabel)
        
        thumbnailImageView.snp.makeConstraints { make in
            make.leading.top.bottom.equalToSuperview()
            make.width.equalTo(80)
            make.height.equalTo(80)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(thumbnailImageView.snp.trailing).offset(12)
            make.trailing.equalToSuperview()
            make.top.equalToSuperview().offset(8)
        }
        
        authorLabel.snp.makeConstraints { make in
            make.leading.equalTo(titleLabel)
            make.top.equalTo(titleLabel.snp.bottom).offset(4)
        }
        
        statsLabel.snp.makeConstraints { make in
            make.leading.equalTo(titleLabel)
            make.bottom.equalToSuperview().inset(8)
        }
        
        snp.makeConstraints { make in
            make.height.equalTo(80)
        }
    }
}

// MARK: - Chart Item View

class ChartItemView: UIView {
    private let rankLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .bold)
        label.textColor = UIColor(hex: "#111827")
        label.textAlignment = .center
        return label
    }()
    
    private let thumbnailImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 6
        imageView.backgroundColor = UIColor(hex: "#E5E7EB")
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let artistLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hex: "#6B7280")
        return label
    }()
    
    private let changeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .bold)
        label.textAlignment = .right
        return label
    }()
    
    init(rank: Int, title: String, artist: String, imageName: String, change: Int) {
        super.init(frame: .zero)
        
        rankLabel.text = "\(rank)"
        titleLabel.text = title
        artistLabel.text = artist
        thumbnailImageView.image = UIImage(named: imageName)
        
        if change > 0 {
            changeLabel.text = "▲ \(change)"
            changeLabel.textColor = UIColor(hex: "#EF4444")
        } else if change < 0 {
            changeLabel.text = "▼ \(abs(change))"
            changeLabel.textColor = UIColor(hex: "#3B82F6")
        } else {
            changeLabel.text = "-"
            changeLabel.textColor = UIColor(hex: "#9CA3AF")
        }
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(rankLabel)
        addSubview(thumbnailImageView)
        addSubview(titleLabel)
        addSubview(artistLabel)
        addSubview(changeLabel)
        
        rankLabel.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
            make.width.equalTo(30)
        }
        
        thumbnailImageView.snp.makeConstraints { make in
            make.leading.equalTo(rankLabel.snp.trailing).offset(12)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(48)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(thumbnailImageView.snp.trailing).offset(12)
            make.trailing.equalTo(changeLabel.snp.leading).offset(-8)
            make.top.equalTo(thumbnailImageView).offset(4)
        }
        
        artistLabel.snp.makeConstraints { make in
            make.leading.equalTo(titleLabel)
            make.trailing.equalTo(titleLabel)
            make.bottom.equalTo(thumbnailImageView).inset(4)
        }
        
        changeLabel.snp.makeConstraints { make in
            make.trailing.equalToSuperview().inset(16)
            make.centerY.equalToSuperview()
            make.width.equalTo(40)
        }
        
        snp.makeConstraints { make in
            make.height.equalTo(68)
        }
    }
}

// MARK: - Vote Card View

class VoteCardView: UIView {
    private let imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 12
        imageView.backgroundColor = UIColor(hex: "#8B5CF6")
        return imageView
    }()
    
    private let overlayView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.black.withAlphaComponent(0.3)
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let groupNameLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .bold)
        label.textColor = .white
        label.textAlignment = .center
        return label
    }()
    
    private let followersLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = .white
        label.textAlignment = .center
        return label
    }()
    
    private let votesLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = .white
        label.textAlignment = .center
        return label
    }()
    
    private let voteButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Vote", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(hex: "#8B5CF6")
        button.layer.cornerRadius = 18
        return button
    }()
    
    init(groupName: String, followers: String, votes: String, imageName: String) {
        super.init(frame: .zero)
        
        groupNameLabel.text = groupName
        followersLabel.text = "👥 \(followers)"
        votesLabel.text = "❤️ \(votes)"
        imageView.image = UIImage(named: imageName)
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(imageView)
        imageView.addSubview(overlayView)
        imageView.addSubview(groupNameLabel)
        imageView.addSubview(followersLabel)
        imageView.addSubview(votesLabel)
        addSubview(voteButton)
        
        imageView.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.height.equalTo(160)
        }
        
        overlayView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        groupNameLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().offset(-15)
        }
        
        followersLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(groupNameLabel.snp.bottom).offset(8)
        }
        
        votesLabel.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalTo(followersLabel.snp.bottom).offset(4)
        }
        
        voteButton.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview().inset(8)
            make.height.equalTo(36)
            make.bottom.equalToSuperview()
        }
    }
}

// MARK: - Event Card View

class EventCardView: UIView {
    private let imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 12
        imageView.backgroundColor = UIColor(hex: "#8B5CF6")
        return imageView
    }()
    
    private let typeBadge: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#8B5CF6")
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let typeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 11, weight: .semibold)
        label.textColor = .white
        return label
    }()
    
    private let dateLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 12, weight: .regular)
        label.textColor = UIColor(hex: "#6B7280")
        return label
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15, weight: .bold)
        label.textColor = UIColor(hex: "#111827")
        label.numberOfLines = 2
        return label
    }()
    
    private let ticketButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Get Tickets", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
        button.setTitleColor(UIColor(hex: "#8B5CF6"), for: .normal)
        button.layer.borderWidth = 1.5
        button.layer.borderColor = UIColor(hex: "#8B5CF6").cgColor
        button.layer.cornerRadius = 20
        button.backgroundColor = .white
        return button
    }()
    
    init(type: String, date: String, title: String, imageName: String) {
        super.init(frame: .zero)
        
        typeLabel.text = type
        dateLabel.text = date
        titleLabel.text = title
        imageView.image = UIImage(named: imageName)
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(imageView)
        addSubview(typeBadge)
        typeBadge.addSubview(typeLabel)
        addSubview(dateLabel)
        addSubview(titleLabel)
        addSubview(ticketButton)
        
        imageView.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.height.equalTo(140)
        }
        
        typeBadge.snp.makeConstraints { make in
            make.top.equalTo(imageView.snp.bottom).offset(12)
            make.leading.equalToSuperview()
            make.height.equalTo(24)
        }
        
        typeLabel.snp.makeConstraints { make in
            make.centerY.equalToSuperview()
            make.horizontalEdges.equalToSuperview().inset(12)
        }
        
        dateLabel.snp.makeConstraints { make in
            make.top.equalTo(typeBadge.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview()
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(dateLabel.snp.bottom).offset(4)
            make.leading.trailing.equalToSuperview()
        }
        
        ticketButton.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview()
            make.height.equalTo(40)
            make.bottom.equalToSuperview()
        }
    }
}

// MARK: - Action Button View

class ActionButtonView: UIView {
    private let iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.tintColor = .white
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 13, weight: .semibold)
        label.textColor = .white
        label.textAlignment = .center
        label.numberOfLines = 2
        return label
    }()
    
    init(icon: String, title: String, color: UIColor) {
        super.init(frame: .zero)
        
        iconImageView.image = UIImage(systemName: icon)
        titleLabel.text = title
        backgroundColor = color
        layer.cornerRadius = 16
        
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(iconImageView)
        addSubview(titleLabel)
        
        iconImageView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.top.equalToSuperview().offset(20)
            make.width.height.equalTo(32)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(iconImageView.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(8)
        }
    }
}
