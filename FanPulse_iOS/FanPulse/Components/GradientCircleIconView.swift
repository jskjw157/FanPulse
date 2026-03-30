//
//  GradientCircleIconView.swift
//  FanPulse
//
//  Created by 김송 on 1/18/26.
//

import UIKit
import SnapKit

final class GradientCircleIconView: UIView {

    private let gradientLayer = CAGradientLayer()
    private let iconImageView = UIImageView()

    init(
        startColor: UIColor,
        endColor: UIColor,
        iconName: String
    ) {
        super.init(frame: .zero)

        // Gradient
        gradientLayer.colors = [
            startColor.cgColor,
            endColor.cgColor
        ]
        gradientLayer.startPoint = CGPoint(x: 0.5, y: 0)
        gradientLayer.endPoint = CGPoint(x: 0.5, y: 1)
        layer.insertSublayer(gradientLayer, at: 0)

        // Icon
        iconImageView.image = UIImage(named: iconName)
        iconImageView.tintColor = .white
        iconImageView.contentMode = .scaleAspectFit

        addSubview(iconImageView)
        
        iconImageView.snp.makeConstraints {
            $0.center.equalToSuperview()
            $0.width.height.equalToSuperview().multipliedBy(0.4)
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()

        gradientLayer.frame = bounds
        
        layer.cornerRadius = bounds.width / 2
        layer.masksToBounds = true
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
