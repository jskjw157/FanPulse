//
//  VotingViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//


import UIKit

final class VotingViewController: BaseViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        configureNavigationBar(type: .vote)
        setNavigationTitle("Voting")
        
        // 나머지 UI 설정
        setupUI()
    }
    
    private func setupUI() {
        // 화면 컨텐츠 추가
    }
}
