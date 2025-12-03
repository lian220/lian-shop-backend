#!/bin/bash

# Git 계정을 lian220으로 변경하는 스크립트

echo "현재 Git 계정 확인 중..."
CURRENT_NAME=$(git config user.name)
CURRENT_EMAIL=$(git config user.email)
echo "  현재 User name: $CURRENT_NAME"
echo "  현재 User email: $CURRENT_EMAIL"
echo ""

if [ "$CURRENT_NAME" = "lian220" ]; then
    echo "⚠️  이미 'lian220' 계정으로 설정되어 있습니다."
    
    # 원격 URL도 확인
    if git remote get-url origin >/dev/null 2>&1; then
        CURRENT_URL=$(git remote get-url origin)
        echo "  현재 원격 URL: $CURRENT_URL"
        
        # SSH URL인지 확인하고 HTTPS로 변경 제안
        if [[ "$CURRENT_URL" == git@github.com:* ]] || [[ "$CURRENT_URL" == ssh://git@github.com/* ]]; then
            echo ""
            echo "🔍 SSH URL이 감지되었습니다."
            echo "   SSH 키가 lian-sfn 계정과 연결되어 있어 권한 문제가 발생할 수 있습니다."
            echo ""
            read -p "HTTPS로 변경하시겠습니까? (y/n): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                # SSH URL을 HTTPS로 변환
                if [[ "$CURRENT_URL" == git@github.com:* ]]; then
                    HTTPS_URL=$(echo "$CURRENT_URL" | sed 's/git@github.com:/https:\/\/github.com\//')
                elif [[ "$CURRENT_URL" == ssh://git@github.com/* ]]; then
                    HTTPS_URL=$(echo "$CURRENT_URL" | sed 's/ssh:\/\/git@github.com\//https:\/\/github.com\//')
                fi
                
                git remote set-url origin "$HTTPS_URL"
                git remote set-url --push origin "$HTTPS_URL"
                echo "✅ 원격 URL이 HTTPS로 변경되었습니다."
                echo "  새 URL: $(git remote get-url origin)"
            fi
        fi
    fi
    
    exit 0
fi

echo "Git 계정을 lian220으로 변경합니다..."

# 사용자 이름 변경
git config user.name "lian220"

# 이메일은 기존 설정 유지
if [ -n "$CURRENT_EMAIL" ]; then
    git config user.email "$CURRENT_EMAIL"
fi

echo "✅ Git 사용자 이름이 'lian220'으로 변경되었습니다."
echo ""
echo "변경된 Git 설정:"
echo "  User name: $(git config user.name)"
echo "  User email: $(git config user.email)"
echo ""

# 원격 URL 확인 및 변경
if git remote get-url origin >/dev/null 2>&1; then
    CURRENT_URL=$(git remote get-url origin)
    echo "현재 원격 URL: $CURRENT_URL"
    
    # SSH URL인지 확인
    if [[ "$CURRENT_URL" == git@github.com:* ]] || [[ "$CURRENT_URL" == ssh://git@github.com/* ]]; then
        echo ""
        echo "🔍 SSH URL이 감지되었습니다."
        echo "   SSH 키가 lian-sfn 계정과 연결되어 있어 권한 문제가 발생할 수 있습니다."
        echo "   HTTPS로 변경하는 것을 권장합니다."
        echo ""
        read -p "HTTPS로 변경하시겠습니까? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            # SSH URL을 HTTPS로 변환
            if [[ "$CURRENT_URL" == git@github.com:* ]]; then
                HTTPS_URL=$(echo "$CURRENT_URL" | sed 's/git@github.com:/https:\/\/github.com\//')
            elif [[ "$CURRENT_URL" == ssh://git@github.com/* ]]; then
                HTTPS_URL=$(echo "$CURRENT_URL" | sed 's/ssh:\/\/git@github.com\//https:\/\/github.com\//')
            fi
            
            git remote set-url origin "$HTTPS_URL"
            git remote set-url --push origin "$HTTPS_URL"
            echo "✅ 원격 URL이 HTTPS로 변경되었습니다."
            echo "  새 URL: $(git remote get-url origin)"
        fi
    else
        echo "✅ 이미 HTTPS URL을 사용하고 있습니다."
    fi
else
    echo "⚠️  원격 저장소가 설정되지 않았습니다."
fi

