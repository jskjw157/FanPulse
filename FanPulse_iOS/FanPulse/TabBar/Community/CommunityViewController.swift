//
//  CommunityViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit
import SnapKit

// MARK: - CommunityViewController

final class CommunityViewController: BaseViewController {
    
    // MARK: - UI Components
    
    private let filterButton: UIButton = {
        let button = UIButton(type: .system)
        button.backgroundColor = UIColor(hex: "#F5F3FF")
        button.layer.cornerRadius = 8
        button.contentHorizontalAlignment = .left
        button.contentEdgeInsets = UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 12)
        
        let titleLabel = UILabel()
        titleLabel.text = "✨ All"
        titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
        titleLabel.textColor = .label
        
        let countLabel = UILabel()
        countLabel.text = "(1234 posts)"
        countLabel.font = .systemFont(ofSize: 14)
        countLabel.textColor = .secondaryLabel
        
        let arrow = UIImageView(image: UIImage(systemName: "chevron.down"))
        arrow.tintColor = .secondaryLabel
        
        let stack = UIStackView(arrangedSubviews: [titleLabel, countLabel, UIView(), arrow])
        stack.axis = .horizontal
        stack.spacing = 4
        stack.isUserInteractionEnabled = false
        
        button.addSubview(stack)
        stack.snp.makeConstraints { $0.edges.equalToSuperview().inset(8) }
        arrow.snp.makeConstraints { $0.width.equalTo(16) }
        
        return button
    }()
    
    private lazy var tabStackView: UIStackView = {
        let stack = UIStackView(arrangedSubviews: [latestPostsButton, popularButton, followingButton])
        stack.axis = .horizontal
        stack.spacing = 8
        stack.distribution = .fillProportionally
        return stack
    }()
    
    private let latestPostsButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Latest Posts", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(hex: "#9333EA")
        button.layer.cornerRadius = 16
        button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 16, bottom: 8, right: 16)
        return button
    }()
    
    private let popularButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("🔥 Popular", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        button.setTitleColor(.label, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 16
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.systemGray5.cgColor
        button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 16, bottom: 8, right: 16)
        return button
    }()
    
    private let followingButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Following", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        button.setTitleColor(.label, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 16
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.systemGray5.cgColor
        button.contentEdgeInsets = UIEdgeInsets(top: 8, left: 16, bottom: 8, right: 16)
        return button
    }()
    
    private let tableView: UITableView = {
        let table = UITableView()
        table.backgroundColor = UIColor(hex: "#F5F5F5")
        table.separatorStyle = .none
        table.showsVerticalScrollIndicator = false
        return table
    }()
    
    private let floatingButton: UIButton = {
        let button = UIButton(type: .system)
        button.backgroundColor = UIColor(hex: "#9333EA")
        button.setImage(UIImage(systemName: "plus"), for: .normal)
        button.tintColor = .white
        button.layer.cornerRadius = 28
        button.layer.shadowColor = UIColor.black.cgColor
        button.layer.shadowOpacity = 0.2
        button.layer.shadowOffset = CGSize(width: 0, height: 4)
        button.layer.shadowRadius = 8
        return button
    }()
    
    // MARK: - Properties
    
    private var posts: [Post] = []
    
    // MARK: - Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        configureNavigationBar(type: .commu)
        setNavigationTitle("Community")
        
        setupUI()
        setupTableView()
        loadMockData()
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        view.addSubview(filterButton)
        view.addSubview(tabStackView)
        view.addSubview(tableView)
        view.addSubview(floatingButton)
        
        filterButton.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide).offset(8)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(44)
        }
        
        tabStackView.snp.makeConstraints {
            $0.top.equalTo(filterButton.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(36)
        }
        
        tableView.snp.makeConstraints {
            $0.top.equalTo(tabStackView.snp.bottom).offset(12)
            $0.leading.trailing.bottom.equalToSuperview()
        }
        
        floatingButton.snp.makeConstraints {
            $0.trailing.equalToSuperview().inset(24)
            $0.bottom.equalTo(view.safeAreaLayoutGuide).inset(16)
            $0.size.equalTo(56)
        }
        
        floatingButton.addTarget(self, action: #selector(didTapFloatingButton), for: .touchUpInside)
    }
    
    private func setupTableView() {
        tableView.delegate = self
        tableView.dataSource = self
        tableView.register(PostCell.self, forCellReuseIdentifier: PostCell.identifier)
    }
    
    private func loadMockData() {
        posts = [
            Post(id: 1, userName: "ARMY_Forever", artistName: "BTS", time: "24분 전", hasVIPBadge: true,
                 content: "BTS 새 앨범 티저 영상 보셨나요? 진짜 너무 기대돼요! 💜 컴백 준비하는 모습 너무 멋있어요 보랏빛이야",
                 hashtags: ["#BTS", "#컴백", "#새앨범"],
                 imageURL: "post_image_1", likes: 1234, comments: 89, shares: 45),
            
            Post(id: 2, userName: "Blink_Girl", artistName: "BLACKPINK", time: "54분 전", hasVIPBadge: false,
                 content: "BLACKPINK 블로투어 티켓 예매 성공했어요! 너무 설레요 ㅠㅠ 같이 가실 분 계시나요?",
                 hashtags: ["#BLACKPINK", "#콘서트"],
                 imageURL: "post_image_2", likes: 856, comments: 67, shares: 34),
            
            Post(id: 3, userName: "Carat_17", artistName: "SEVENTEEN", time: "1시간 전", hasVIPBadge: true,
                 content: "SEVENTEEN 신곡 연습 영상 짱이었어! 칼군무 진짜 미쳤다... 13명이 한 명처럼 움직이는 게 신기해요",
                 hashtags: ["#SEVENTEEN", "#칼군무"],
                 imageURL: "post_image_3", likes: 645, comments: 52, shares: 28),
            
            Post(id: 4, userName: "Bunny_Fan", artistName: "NewJeans", time: "3시간 전", hasVIPBadge: false,
                 content: "New Jeans 신곡 컴백 1주일 돌파! 쌉 우리 토끼들 최고야 ㅠㅠㅠ",
                 hashtags: ["#NewJeans"],
                 imageURL: "post_image_4", likes: 523, comments: 41, shares: 19),
            
            Post(id: 5, userName: "Stay_Forever", artistName: "Stray Kids", time: "6시간 전", hasVIPBadge: true,
                 content: "Stray Kids 지지자 전체 천재돌... 이번 앨범도 망했군 감동에요",
                 hashtags: ["#StrayKids"],
                 imageURL: "post_image_5", likes: 412, comments: 35, shares: 15),
            
            Post(id: 6, userName: "Once_Love", artistName: "TWICE", time: "8시간 전", hasVIPBadge: false,
                 content: "TWICE 콘서트 셋리스트 예상해봤어요! 어떤 곡을 나올까요? 🎵",
                 hashtags: ["#TWICE", "#콘서트"],
                 imageURL: "post_image_6", likes: 389, comments: 48, shares: 12)
        ]
        tableView.reloadData()
    }
    
    // MARK: - Actions
    
    @objc private func didTapFloatingButton() {
        let createPostVC = CreatePostViewController()
        createPostVC.modalPresentationStyle = .fullScreen
        present(createPostVC, animated: true)
    }
}

// MARK: - UITableViewDelegate, UITableViewDataSource

extension CommunityViewController: UITableViewDelegate, UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return posts.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: PostCell.identifier, for: indexPath) as? PostCell else {
            return UITableViewCell()
        }
        cell.configure(with: posts[indexPath.row])
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let detailVC = PostDetailViewController(post: posts[indexPath.row])
        navigationController?.pushViewController(detailVC, animated: true)
    }
}

// MARK: - PostCell

final class PostCell: UITableViewCell {
    
    static let identifier = "PostCell"
    
    // MARK: - UI Components
    
    private let containerView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 12
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.05
        view.layer.shadowOffset = CGSize(width: 0, height: 2)
        view.layer.shadowRadius = 4
        return view
    }()
    
    private let profileImageView: UIImageView = {
        let iv = UIImageView()
        iv.contentMode = .scaleAspectFill
        iv.clipsToBounds = true
        iv.layer.cornerRadius = 20
        iv.backgroundColor = UIColor(hex: "#F472B6")
        return iv
    }()
    
    private let userNameLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15, weight: .semibold)
        label.textColor = .label
        return label
    }()
    
    private let vipBadge: UILabel = {
        let label = UILabel()
        label.text = "VIP"
        label.font = .systemFont(ofSize: 10, weight: .bold)
        label.textColor = .white
        label.backgroundColor = UIColor(hex: "#F59E0B")
        label.textAlignment = .center
        label.layer.cornerRadius = 8
        label.clipsToBounds = true
        label.isHidden = true
        return label
    }()
    
    private let artistLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#9333EA")
        return label
    }()
    
    private let timeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 13)
        label.textColor = .secondaryLabel
        return label
    }()
    
    private let moreButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("⋮", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 20, weight: .bold)
        button.setTitleColor(.systemGray3, for: .normal)
        return button
    }()
    
    private let contentLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = .label
        label.numberOfLines = 0
        return label
    }()
    
    private let hashtagLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 13)
        label.textColor = UIColor(hex: "#9333EA")
        label.numberOfLines = 1
        return label
    }()
    
    private let postImageView: UIImageView = {
        let iv = UIImageView()
        iv.contentMode = .scaleAspectFill
        iv.clipsToBounds = true
        iv.layer.cornerRadius = 8
        iv.backgroundColor = UIColor(hex: "#F5F3FF")
        return iv
    }()
    
    private lazy var interactionStackView: UIStackView = {
        let stack = UIStackView(arrangedSubviews: [likeButton, commentButton, shareButton, UIView(), bookmarkButton])
        stack.axis = .horizontal
        stack.spacing = 16
        stack.alignment = .center
        return stack
    }()
    
    private let likeButton = PostCell.makeInteractionButton(imageName: "heart", count: 0)
    private let commentButton = PostCell.makeInteractionButton(imageName: "message", count: 0)
    private let shareButton = PostCell.makeInteractionButton(imageName: "square.and.arrow.up", count: 0)
    private let bookmarkButton: UIButton = {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "bookmark"), for: .normal)
        button.tintColor = .systemGray2
        return button
    }()
    
    // MARK: - Init
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        backgroundColor = .clear
        selectionStyle = .none
        
        contentView.addSubview(containerView)
        
        [profileImageView, userNameLabel, vipBadge, artistLabel, timeLabel, moreButton,
         contentLabel, hashtagLabel, postImageView, interactionStackView].forEach {
            containerView.addSubview($0)
        }
        
        containerView.snp.makeConstraints {
            $0.edges.equalToSuperview().inset(UIEdgeInsets(top: 6, left: 16, bottom: 6, right: 16))
        }
        
        profileImageView.snp.makeConstraints {
            $0.top.leading.equalToSuperview().inset(16)
            $0.size.equalTo(40)
        }
        
        userNameLabel.snp.makeConstraints {
            $0.top.equalTo(profileImageView)
            $0.leading.equalTo(profileImageView.snp.trailing).offset(12)
        }
        
        vipBadge.snp.makeConstraints {
            $0.centerY.equalTo(userNameLabel)
            $0.leading.equalTo(userNameLabel.snp.trailing).offset(6)
            $0.width.equalTo(32)
            $0.height.equalTo(16)
        }
        
        artistLabel.snp.makeConstraints {
            $0.top.equalTo(userNameLabel.snp.bottom).offset(2)
            $0.leading.equalTo(userNameLabel)
        }
        
        timeLabel.snp.makeConstraints {
            $0.centerY.equalTo(artistLabel)
            $0.leading.equalTo(artistLabel.snp.trailing).offset(8)
        }
        
        moreButton.snp.makeConstraints {
            $0.top.trailing.equalToSuperview().inset(16)
            $0.size.equalTo(24)
        }
        
        contentLabel.snp.makeConstraints {
            $0.top.equalTo(profileImageView.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        hashtagLabel.snp.makeConstraints {
            $0.top.equalTo(contentLabel.snp.bottom).offset(8)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        postImageView.snp.makeConstraints {
            $0.top.equalTo(hashtagLabel.snp.bottom).offset(8)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(200)
        }
        
        interactionStackView.snp.makeConstraints {
            $0.top.equalTo(postImageView.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.bottom.equalToSuperview().inset(16)
            $0.height.equalTo(32)
        }
    }
    
    // MARK: - Configure
    
    func configure(with post: Post) {
        userNameLabel.text = post.userName
        artistLabel.text = post.artistName
        timeLabel.text = post.time
        vipBadge.isHidden = !post.hasVIPBadge
        contentLabel.text = post.content
        hashtagLabel.text = post.hashtags.joined(separator: " ")
        
        if let imageName = post.imageURL {
            postImageView.image = UIImage(named: imageName)
        }
        
        updateInteractionButton(likeButton, count: post.likes)
        updateInteractionButton(commentButton, count: post.comments)
        updateInteractionButton(shareButton, count: post.shares)
    }
    
    private func updateInteractionButton(_ button: UIButton, count: Int) {
        if let label = button.subviews.compactMap({ $0 as? UILabel }).first {
            label.text = "\(count)"
        }
    }
    
    private static func makeInteractionButton(imageName: String, count: Int) -> UIButton {
        let button = UIButton(type: .system)
        button.tintColor = .systemGray2
        
        let imageView = UIImageView(image: UIImage(systemName: imageName))
        imageView.tintColor = .systemGray2
        
        let label = UILabel()
        label.text = "\(count)"
        label.font = .systemFont(ofSize: 13, weight: .medium)
        label.textColor = .systemGray2
        
        let stack = UIStackView(arrangedSubviews: [imageView, label])
        stack.axis = .horizontal
        stack.spacing = 4
        stack.isUserInteractionEnabled = false
        
        button.addSubview(stack)
        stack.snp.makeConstraints { $0.edges.equalToSuperview() }
        imageView.snp.makeConstraints { $0.size.equalTo(20) }
        
        return button
    }
}
