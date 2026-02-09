//
//  PostDetailViewController.swift
//  FanPulse
//
//  Created by 김송 on 2/1/26.
//

import UIKit
import SnapKit
import Kingfisher

// MARK: - PostDetailViewController

final class PostDetailViewController: UIViewController {
    
    private let scrollView = UIScrollView()
    private let contentView = UIView()
    
    private let post: Post
    
    private let profileImageView: UIImageView = {
        let iv = UIImageView()
        iv.contentMode = .scaleAspectFill
        iv.clipsToBounds = true
        iv.layer.cornerRadius = 25
        iv.backgroundColor = UIColor(hex: "#F472B6")
        return iv
    }()
    
    private let userNameLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 16, weight: .semibold)
        return label
    }()
    
    private let vipBadge: UILabel = {
        let label = UILabel()
        label.text = "VIP"
        label.font = .systemFont(ofSize: 10, weight: .bold)
        label.textColor = .white
        label.backgroundColor = UIColor(hex: "#C084FC")
        label.textAlignment = .center
        label.layer.cornerRadius = 8
        label.clipsToBounds = true
        return label
    }()
    
    private let timeLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = .secondaryLabel
        return label
    }()
    
    private let followButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("팔로우", for: .normal)
        button.setTitleColor(UIColor(hex: "#9333EA"), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
        button.backgroundColor = .white
        button.layer.cornerRadius = 16
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor(hex: "#9333EA").cgColor
        return button
    }()
    
    private let contentLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.numberOfLines = 0
        return label
    }()
    
    private let hashtagLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14)
        label.textColor = UIColor(hex: "#9333EA")
        label.numberOfLines = 0
        return label
    }()
    
    private let postImageView: UIImageView = {
        let iv = UIImageView()
        iv.contentMode = .scaleAspectFill
        iv.clipsToBounds = true
        iv.layer.cornerRadius = 12
        return iv
    }()
    
    private let likeButton = PostDetailViewController.makeInteractionButton(imageName: "heart", text: "좋아요", count: 0)
    private let commentButton = PostDetailViewController.makeInteractionButton(imageName: "message", text: "댓글", count: 0)
    private let shareButton = PostDetailViewController.makeInteractionButton(imageName: "square.and.arrow.up", text: "공유", count: 0)
    private let bookmarkButton: UIButton = {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "bookmark"), for: .normal)
        button.tintColor = .label
        return button
    }()
    
    private let commentHeaderLabel: UILabel = {
        let label = UILabel()
        label.text = "댓글 3"
        label.font = .systemFont(ofSize: 16, weight: .bold)
        return label
    }()
    
    private let commentInputField: UITextField = {
        let tf = UITextField()
        tf.placeholder = "댓글을 입력하세요..."
        tf.font = .systemFont(ofSize: 14)
        tf.borderStyle = .none
        tf.layer.cornerRadius = 20
        tf.backgroundColor = UIColor(hex: "#F5F5F5")
        tf.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
        tf.leftViewMode = .always
        return tf
    }()
    
    private let sendButton: UIButton = {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "paperplane.fill"), for: .normal)
        button.tintColor = .white
        button.backgroundColor = UIColor(hex: "#9333EA")
        button.layer.cornerRadius = 20
        return button
    }()
    
    init(post: Post) {
        self.post = post
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupNavigationBar()
        setupUI()
        configure()
    }
    
    private func setupNavigationBar() {
        navigationItem.title = "게시글"
        navigationItem.rightBarButtonItem = UIBarButtonItem(
            image: UIImage(systemName: "ellipsis"),
            style: .plain,
            target: self,
            action: #selector(didTapMore)
        )
        navigationItem.rightBarButtonItem?.tintColor = .label
    }
    
    private func setupUI() {
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        
        [profileImageView, userNameLabel, vipBadge, timeLabel, followButton,
         contentLabel, hashtagLabel, postImageView,
         likeButton, commentButton, shareButton, bookmarkButton,
         commentHeaderLabel, commentInputField, sendButton].forEach {
            contentView.addSubview($0)
        }
        
        scrollView.snp.makeConstraints {
            $0.edges.equalTo(view.safeAreaLayoutGuide)
        }
        
        contentView.snp.makeConstraints {
            $0.edges.equalToSuperview()
            $0.width.equalToSuperview()
        }
        
        profileImageView.snp.makeConstraints {
            $0.top.leading.equalToSuperview().inset(16)
            $0.size.equalTo(50)
        }
        
        userNameLabel.snp.makeConstraints {
            $0.top.equalTo(profileImageView).offset(4)
            $0.leading.equalTo(profileImageView.snp.trailing).offset(12)
        }
        
        vipBadge.snp.makeConstraints {
            $0.centerY.equalTo(userNameLabel)
            $0.leading.equalTo(userNameLabel.snp.trailing).offset(6)
            $0.width.equalTo(32)
            $0.height.equalTo(16)
        }
        
        timeLabel.snp.makeConstraints {
            $0.top.equalTo(userNameLabel.snp.bottom).offset(2)
            $0.leading.equalTo(userNameLabel)
        }
        
        followButton.snp.makeConstraints {
            $0.centerY.equalTo(profileImageView)
            $0.trailing.equalToSuperview().inset(16)
            $0.width.equalTo(80)
            $0.height.equalTo(32)
        }
        
        contentLabel.snp.makeConstraints {
            $0.top.equalTo(profileImageView.snp.bottom).offset(16)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        hashtagLabel.snp.makeConstraints {
            $0.top.equalTo(contentLabel.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        postImageView.snp.makeConstraints {
            $0.top.equalTo(hashtagLabel.snp.bottom).offset(16)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(300)
        }
        
        let buttonStack = UIStackView(arrangedSubviews: [likeButton, commentButton, shareButton])
        buttonStack.axis = .horizontal
        buttonStack.spacing = 24
        contentView.addSubview(buttonStack)
        
        buttonStack.snp.makeConstraints {
            $0.top.equalTo(postImageView.snp.bottom).offset(16)
            $0.leading.equalToSuperview().inset(16)
        }
        
        bookmarkButton.snp.makeConstraints {
            $0.centerY.equalTo(buttonStack)
            $0.trailing.equalToSuperview().inset(16)
            $0.size.equalTo(24)
        }
        
        commentHeaderLabel.snp.makeConstraints {
            $0.top.equalTo(buttonStack.snp.bottom).offset(24)
            $0.leading.equalToSuperview().inset(16)
        }
        
        addMockComments()
        
        commentInputField.snp.makeConstraints {
            $0.leading.equalToSuperview().inset(16)
            $0.trailing.equalTo(sendButton.snp.leading).offset(-8)
            $0.bottom.equalToSuperview().inset(16)
            $0.height.equalTo(40)
        }
        
        sendButton.snp.makeConstraints {
            $0.trailing.equalToSuperview().inset(16)
            $0.centerY.equalTo(commentInputField)
            $0.size.equalTo(40)
        }
    }
    
    private func addMockComments() {
        let comments = [
            ("Blink_Girl", "1시간 전", "저도 티켓 보고 스트 탔어요! 이번 컨셉 진짜 좋은 것 같아요"),
            ("Kpop_Lover", "30분 전", "버버터터 기대되네요 ㅠㅠ 빨리 발매일 공개했으면 좋겠어요"),
            ("Music_Fan", "15분 전", "이번 앨범도 빌보드 1위 가즈아 👍")
        ]
        
        var lastView: UIView = commentHeaderLabel
        
        for (name, time, text) in comments {
            let commentView = createCommentView(name: name, time: time, text: text)
            contentView.addSubview(commentView)
            
            commentView.snp.makeConstraints {
                $0.top.equalTo(lastView.snp.bottom).offset(16)
                $0.leading.trailing.equalToSuperview().inset(16)
            }
            
            lastView = commentView
        }
        
        commentInputField.snp.remakeConstraints {
            $0.top.equalTo(lastView.snp.bottom).offset(24)
            $0.leading.equalToSuperview().inset(16)
            $0.trailing.equalTo(sendButton.snp.leading).offset(-8)
            $0.bottom.equalToSuperview().inset(16)
            $0.height.equalTo(40)
        }
    }
    
    private func createCommentView(name: String, time: String, text: String) -> UIView {
        let container = UIView()
        
        let profileImage = UIImageView()
        profileImage.backgroundColor = UIColor(hex: "#EC4899")
        profileImage.layer.cornerRadius = 16
        profileImage.clipsToBounds = true
        
        let nameLabel = UILabel()
        nameLabel.text = name
        nameLabel.font = .systemFont(ofSize: 14, weight: .semibold)
        
        let timeLabel = UILabel()
        timeLabel.text = time
        timeLabel.font = .systemFont(ofSize: 12)
        timeLabel.textColor = .secondaryLabel
        
        let textLabel = UILabel()
        textLabel.text = text
        textLabel.font = .systemFont(ofSize: 14)
        textLabel.numberOfLines = 0
        
        let likeBtn = UIButton(type: .system)
        likeBtn.setImage(UIImage(systemName: "heart"), for: .normal)
        likeBtn.tintColor = .systemGray3
        
        let likeCount = UILabel()
        likeCount.text = "\(Int.random(in: 10...50))"
        likeCount.font = .systemFont(ofSize: 12)
        likeCount.textColor = .secondaryLabel
        
        let replyBtn = UIButton(type: .system)
        replyBtn.setTitle("답글", for: .normal)
        replyBtn.titleLabel?.font = .systemFont(ofSize: 12)
        replyBtn.setTitleColor(.secondaryLabel, for: .normal)
        
        [profileImage, nameLabel, timeLabel, textLabel, likeBtn, likeCount, replyBtn].forEach {
            container.addSubview($0)
        }
        
        profileImage.snp.makeConstraints {
            $0.top.leading.equalToSuperview()
            $0.size.equalTo(32)
        }
        
        nameLabel.snp.makeConstraints {
            $0.top.equalTo(profileImage)
            $0.leading.equalTo(profileImage.snp.trailing).offset(8)
        }
        
        timeLabel.snp.makeConstraints {
            $0.centerY.equalTo(nameLabel)
            $0.leading.equalTo(nameLabel.snp.trailing).offset(8)
        }
        
        textLabel.snp.makeConstraints {
            $0.top.equalTo(nameLabel.snp.bottom).offset(4)
            $0.leading.equalTo(nameLabel)
            $0.trailing.equalToSuperview()
        }
        
        likeBtn.snp.makeConstraints {
            $0.top.equalTo(textLabel.snp.bottom).offset(8)
            $0.leading.equalTo(nameLabel)
            $0.size.equalTo(16)
            $0.bottom.equalToSuperview()
        }
        
        likeCount.snp.makeConstraints {
            $0.centerY.equalTo(likeBtn)
            $0.leading.equalTo(likeBtn.snp.trailing).offset(4)
        }
        
        replyBtn.snp.makeConstraints {
            $0.centerY.equalTo(likeBtn)
            $0.leading.equalTo(likeCount.snp.trailing).offset(12)
        }
        
        return container
    }
    
    private func configure() {
        userNameLabel.text = post.userName
        timeLabel.text = post.time
        vipBadge.isHidden = !post.hasVIPBadge
        contentLabel.text = post.content
        hashtagLabel.text = post.hashtags.joined(separator: " ")
        
        if let url = URL(string: imageURL) {
            postImageView.kf.setImage(with: url)
        }
        
        updateButton(likeButton, count: post.likes)
        updateButton(commentButton, count: post.comments)
        updateButton(shareButton, count: post.shares)
    }
    
    private func updateButton(_ button: UIButton, count: Int) {
        if let label = button.subviews.compactMap({ $0 as? UILabel }).first {
            label.text = "\(count)"
        }
    }
    
    @objc private func didTapMore() {
        print("More tapped")
    }
    
    private static func makeInteractionButton(imageName: String, text: String, count: Int) -> UIButton {
        let button = UIButton(type: .system)
        button.tintColor = .label
        
        let imageView = UIImageView(image: UIImage(systemName: imageName))
        imageView.tintColor = .label
        
        let textLabel = UILabel()
        textLabel.text = text
        textLabel.font = .systemFont(ofSize: 13)
        textLabel.textColor = .label
        
        let countLabel = UILabel()
        countLabel.text = "\(count)"
        countLabel.font = .systemFont(ofSize: 13, weight: .medium)
        countLabel.textColor = .secondaryLabel
        
        let stack = UIStackView(arrangedSubviews: [imageView, textLabel])
        stack.axis = .vertical
        stack.spacing = 4
        stack.alignment = .center
        stack.isUserInteractionEnabled = false
        
        button.addSubview(stack)
        button.addSubview(countLabel)
        
        stack.snp.makeConstraints {
            $0.top.leading.trailing.equalToSuperview()
        }
        imageView.snp.makeConstraints { $0.size.equalTo(24) }
        
        countLabel.snp.makeConstraints {
            $0.top.equalTo(stack.snp.bottom)
            $0.centerX.equalToSuperview()
            $0.bottom.equalToSuperview()
        }
        
        return button
    }
}
