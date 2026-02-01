//
//  CreatePostViewController.swift
//  FanPulse
//
//  Created by 김송 on 2/1/26.
//

import UIKit
import SnapKit

// MARK: - CreatePostViewController

final class CreatePostViewController: UIViewController {
    
    private let scrollView = UIScrollView()
    private let contentView = UIView()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "게시글 작성"
        label.font = .systemFont(ofSize: 20, weight: .bold)
        label.textAlignment = .center
        return label
    }()
    
    private let closeButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("취소", for: .normal)
        button.setTitleColor(.label, for: .normal)
        return button
    }()
    
    private let submitButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("게시", for: .normal)
        button.setTitleColor(UIColor(hex: "#9333EA"), for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
        return button
    }()
    
    private let artistLabel: UILabel = {
        let label = UILabel()
        label.text = "아티스트 선택 *"
        label.font = .systemFont(ofSize: 15, weight: .semibold)
        return label
    }()
    
    private let artistButtons: [UIButton] = [
        CreatePostViewController.makeArtistButton(title: "BTS"),
        CreatePostViewController.makeArtistButton(title: "BLACKPINK"),
        CreatePostViewController.makeArtistButton(title: "SEVENTEEN"),
        CreatePostViewController.makeArtistButton(title: "NewJeans"),
        CreatePostViewController.makeArtistButton(title: "Stray Kids"),
        CreatePostViewController.makeArtistButton(title: "TWICE")
    ]
    
    private let contentLabel: UILabel = {
        let label = UILabel()
        label.text = "내용 *"
        label.font = .systemFont(ofSize: 15, weight: .semibold)
        return label
    }()
    
    private let contentTextView: UITextView = {
        let tv = UITextView()
        tv.font = .systemFont(ofSize: 15)
        tv.textColor = .placeholderText
        tv.text = "팬 여러분과 공유하고 싶은 이야기를 작성해주세요..."
        tv.layer.cornerRadius = 8
        tv.layer.borderWidth = 1
        tv.layer.borderColor = UIColor.systemGray4.cgColor
        tv.textContainerInset = UIEdgeInsets(top: 12, left: 8, bottom: 12, right: 8)
        return tv
    }()
    
    private let charCountLabel: UILabel = {
        let label = UILabel()
        label.text = "0/500"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .secondaryLabel
        label.textAlignment = .right
        return label
    }()
    
    private let imageLabel: UILabel = {
        let label = UILabel()
        label.text = "이미지 첨부"
        label.font = .systemFont(ofSize: 15, weight: .semibold)
        return label
    }()
    
    private let imageUploadView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F5F5F5")
        view.layer.cornerRadius = 8
        view.layer.borderWidth = 1
        view.layer.borderColor = UIColor.systemGray4.cgColor
//        view.layer.borderStyle = .init(rawValue: 2) ?? .solid
        return view
    }()
    
    private let imageIcon: UIImageView = {
        let iv = UIImageView(image: UIImage(systemName: "photo.badge.plus"))
        iv.tintColor = .systemGray3
        iv.contentMode = .scaleAspectFit
        return iv
    }()
    
    private let imageUploadLabel: UILabel = {
        let label = UILabel()
        label.text = "이미지 추가 (최대 5장)"
        label.font = .systemFont(ofSize: 14)
        label.textColor = .secondaryLabel
        label.textAlignment = .center
        return label
    }()
    
    private let tagLabel: UILabel = {
        let label = UILabel()
        label.text = "태그 (최대 5개)"
        label.font = .systemFont(ofSize: 15, weight: .semibold)
        return label
    }()
    
    private let tagTextField: UITextField = {
        let tf = UITextField()
        tf.placeholder = "태그 입력 후 엔터"
        tf.font = .systemFont(ofSize: 15)
        tf.borderStyle = .none
        tf.layer.cornerRadius = 8
        tf.layer.borderWidth = 1
        tf.layer.borderColor = UIColor.systemGray4.cgColor
        tf.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 12, height: 0))
        tf.leftViewMode = .always
        return tf
    }()
    
    private let addTagButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("추가", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(hex: "#9333EA")
        button.layer.cornerRadius = 16
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        return button
    }()
    
    private let guideView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#F5F3FF")
        view.layer.cornerRadius = 8
        return view
    }()
    
    private let guideIcon: UIImageView = {
        let iv = UIImageView(image: UIImage(systemName: "info.circle"))
        iv.tintColor = UIColor(hex: "#9333EA")
        return iv
    }()
    
    private let guideLabel: UILabel = {
        let label = UILabel()
        label.text = "게시글 작성 가이드\n• 타인을 존중하는 내용을 작성해주세요\n• 욕설, 비방, 허위사실은 삭제될 수 있습니다\n• 저작권을 침해하는 콘텐츠는 게시할 수 없습니다"
        label.font = .systemFont(ofSize: 12)
        label.textColor = .secondaryLabel
        label.numberOfLines = 0
        return label
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupUI()
        setupActions()
    }
    
    private func setupUI() {
        view.addSubview(closeButton)
        view.addSubview(titleLabel)
        view.addSubview(submitButton)
        view.addSubview(scrollView)
        
        scrollView.addSubview(contentView)
        
        [artistLabel, contentLabel, contentTextView, charCountLabel,
         imageLabel, imageUploadView, tagLabel, tagTextField, addTagButton, guideView].forEach {
            contentView.addSubview($0)
        }
        
        artistButtons.forEach { contentView.addSubview($0) }
        
        imageUploadView.addSubview(imageIcon)
        imageUploadView.addSubview(imageUploadLabel)
        
        guideView.addSubview(guideIcon)
        guideView.addSubview(guideLabel)
        
        closeButton.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide).offset(8)
            $0.leading.equalToSuperview().inset(16)
        }
        
        titleLabel.snp.makeConstraints {
            $0.centerY.equalTo(closeButton)
            $0.centerX.equalToSuperview()
        }
        
        submitButton.snp.makeConstraints {
            $0.centerY.equalTo(closeButton)
            $0.trailing.equalToSuperview().inset(16)
        }
        
        scrollView.snp.makeConstraints {
            $0.top.equalTo(titleLabel.snp.bottom).offset(16)
            $0.leading.trailing.bottom.equalToSuperview()
        }
        
        contentView.snp.makeConstraints {
            $0.edges.equalToSuperview()
            $0.width.equalToSuperview()
        }
        
        artistLabel.snp.makeConstraints {
            $0.top.equalToSuperview().offset(16)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        for (index, button) in artistButtons.enumerated() {
            let row = index / 3
            let col = index % 3
            
            button.snp.makeConstraints {
                $0.top.equalTo(artistLabel.snp.bottom).offset(12 + row * 44)
                $0.leading.equalToSuperview().inset(16 + col * 120)
                $0.width.equalTo(110)
                $0.height.equalTo(36)
            }
        }
        
        contentLabel.snp.makeConstraints {
            $0.top.equalTo(artistButtons[3].snp.bottom).offset(24)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        contentTextView.snp.makeConstraints {
            $0.top.equalTo(contentLabel.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(200)
        }
        
        charCountLabel.snp.makeConstraints {
            $0.top.equalTo(contentTextView.snp.bottom).offset(8)
            $0.trailing.equalToSuperview().inset(16)
        }
        
        imageLabel.snp.makeConstraints {
            $0.top.equalTo(charCountLabel.snp.bottom).offset(24)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        imageUploadView.snp.makeConstraints {
            $0.top.equalTo(imageLabel.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.height.equalTo(120)
        }
        
        imageIcon.snp.makeConstraints {
            $0.centerX.equalToSuperview()
            $0.centerY.equalToSuperview().offset(-12)
            $0.size.equalTo(40)
        }
        
        imageUploadLabel.snp.makeConstraints {
            $0.top.equalTo(imageIcon.snp.bottom).offset(8)
            $0.centerX.equalToSuperview()
        }
        
        tagLabel.snp.makeConstraints {
            $0.top.equalTo(imageUploadView.snp.bottom).offset(24)
            $0.leading.trailing.equalToSuperview().inset(16)
        }
        
        tagTextField.snp.makeConstraints {
            $0.top.equalTo(tagLabel.snp.bottom).offset(12)
            $0.leading.equalToSuperview().inset(16)
            $0.trailing.equalTo(addTagButton.snp.leading).offset(-8)
            $0.height.equalTo(44)
        }
        
        addTagButton.snp.makeConstraints {
            $0.centerY.equalTo(tagTextField)
            $0.trailing.equalToSuperview().inset(16)
            $0.width.equalTo(60)
            $0.height.equalTo(32)
        }
        
        guideView.snp.makeConstraints {
            $0.top.equalTo(tagTextField.snp.bottom).offset(24)
            $0.leading.trailing.equalToSuperview().inset(16)
            $0.bottom.equalToSuperview().inset(24)
        }
        
        guideIcon.snp.makeConstraints {
            $0.top.leading.equalToSuperview().inset(12)
            $0.size.equalTo(20)
        }
        
        guideLabel.snp.makeConstraints {
            $0.top.equalTo(guideIcon.snp.bottom).offset(8)
            $0.leading.trailing.bottom.equalToSuperview().inset(12)
        }
    }
    
    private func setupActions() {
        closeButton.addTarget(self, action: #selector(didTapClose), for: .touchUpInside)
        submitButton.addTarget(self, action: #selector(didTapSubmit), for: .touchUpInside)
    }
    
    @objc private func didTapClose() {
        dismiss(animated: true)
    }
    
    @objc private func didTapSubmit() {
        dismiss(animated: true)
    }
    
    private static func makeArtistButton(title: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.setTitleColor(.label, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        button.backgroundColor = UIColor(hex: "#F5F5F5")
        button.layer.cornerRadius = 18
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.systemGray5.cgColor
        return button
    }
}
