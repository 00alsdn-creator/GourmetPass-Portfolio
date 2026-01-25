/* GourmetPass 통합 회원 관리 스크립트 (가입/수정/탈퇴/알림/가게정보) */

(function($) {
    // 1. 전역 상태 변수 설정
    let isIdChecked = false; 
    let isPwMatched = false;
    let isEmailChecked = false; 
    let authCode = "";         
    let timerInterval;
    let initialEmail = ""; 

    $(document).ready(function() {
        
        // [A] 로그인/로그아웃 알림 (기존 member.js 이관)
        const authMsgBox = $("#auth-msg");
        if(authMsgBox.length > 0) {
            const error = authMsgBox.data("error");
            const logout = authMsgBox.data("logout");
            if (error) alert("아이디 또는 비밀번호가 잘못되었습니다.");
            if (logout) alert("성공적으로 로그아웃되었습니다. 이용해 주셔서 감사합니다.");
        }

        // [B] 초기 데이터 세팅 및 수정 모드 감지
        initialEmail = $("#user_email").val() || ""; 
        
        // 수정 페이지 대응: 아이디가 readonly라면 중복확인 및 이메일인증 초기값 통과
        if($("#user_id").prop("readonly")) {
            isIdChecked = true;
            isEmailChecked = true; 
            console.log("Mode: Edit Mode Detected (Validation Adjusted)");
        }

        // 1. 아이디 중복 확인 (AJAX)
        $("#btnIdCheck").click(function() {
            if($("#user_id").prop("readonly")) return;

            const userId = $("#user_id").val();
            if(!userId || userId.length < 3) { 
                alert("아이디는 3글자 이상 입력해주세요."); 
                return; 
            }

            const ajaxData = { user_id: userId };
            if (typeof APP_CONFIG !== 'undefined') {
                ajaxData[APP_CONFIG.csrfName] = APP_CONFIG.csrfToken;
            }

            $.ajax({
                url: (typeof APP_CONFIG !== 'undefined' ? APP_CONFIG.contextPath : "") + "/member/idCheck",
                type: "POST",
                data: ajaxData,
                success: function(res) {
                    if(res === "success") { 
                        $("#idCheckMsg").html("<span class='msg-ok'>사용 가능한 아이디입니다.</span>"); 
                        isIdChecked = true; 
                    } else { 
                        $("#idCheckMsg").html("<span class='msg-no'>이미 사용 중인 아이디입니다.</span>");
                        isIdChecked = false; 
                    }
                },
                error: function() { alert("서버 통신 오류가 발생했습니다."); }
            });
        });

        // 아이디 입력 시 상태 초기화 (가입 모드 전용)
        $("#user_id").on("input", function() {
            if(!$(this).prop("readonly")) {
                isIdChecked = false; 
                $("#idCheckMsg").text("");
            }
        });

        // 2. 비밀번호 일치 확인 (실시간)
        $("#user_pw, #user_pw_confirm").on("keyup change input", function() {
            const pw = $("#user_pw").val();
            const pwConfirm = $("#user_pw_confirm").val();
            
            // 수정 시: 변경하지 않을 경우(둘 다 공백) 통과
            if(pw === "" && pwConfirm === "") { 
                $("#pwCheckMsg").text(""); 
                if($("#user_id").prop("readonly")) isPwMatched = true; 
                return; 
            }
            
            if(pw === pwConfirm) { 
                $("#pwCheckMsg").html("<span class='msg-ok'>비밀번호가 일치합니다.</span>"); 
                isPwMatched = true; 
            } else { 
                $("#pwCheckMsg").html("<span class='msg-no'>비밀번호가 일치하지 않습니다.</span>"); 
                isPwMatched = false; 
            }
        });

        // 3. 이메일 변경 감지 (수정 모드 대응)
        $("#user_email").on("input change", function() {
            const currentEmail = $(this).val();
            if (currentEmail === initialEmail) {
                $("#emailMsg").text("");
                isEmailChecked = true; 
            } else {
                $("#emailMsg").html("<span class='msg-no'>이메일 변경 시 인증이 필요합니다.</span>");
                isEmailChecked = false;
            }
        });

        // 4. 이메일 인증코드 발송
        $("#btnEmailAuth").click(function() {
            const email = $("#user_email").val();
            if(!email) { alert("이메일을 입력해주세요."); return; }

            const ajaxData = { email: email };
            if (typeof APP_CONFIG !== 'undefined') {
                ajaxData[APP_CONFIG.csrfName] = APP_CONFIG.csrfToken;
            }

            $.ajax({
                url: (typeof APP_CONFIG !== 'undefined' ? APP_CONFIG.contextPath : "") + "/member/emailAuth",
                type: "POST",
                data: ajaxData,
                success: function(res) {
                    alert("인증코드가 발송되었습니다.");
                    authCode = res; 
                    $("#auth_code").prop("disabled", false).val("").focus();
                    startTimer();
                },
                error: function() { alert("메일 발송에 실패했습니다."); }
            });
        });

        // 5. 인증번호 실시간 확인
        $("#auth_code").on("keyup", function() {
            const inputCode = $(this).val();
            if(inputCode.length === 6) {
                if(Number(inputCode) === authCode) { 
                    $("#authMsg").html("<span class='msg-ok'>인증 성공</span>");
                    clearInterval(timerInterval);
                    $("#timer").text("");
                    $("#btnEmailAuth, #auth_code").prop("disabled", true);
                    $("#user_email").prop("readonly", true);
                    isEmailChecked = true;
                    initialEmail = $("#user_email").val(); // 인증된 이메일을 기준값으로 갱신
                } else {
                    $("#authMsg").html("<span class='msg-no'>인증번호가 일치하지 않습니다.</span>");
                    isEmailChecked = false;
                }
            }
        });

        // 타이머 기능
        function startTimer() {
            let time = 180;
            clearInterval(timerInterval);
            timerInterval = setInterval(function() {
                let min = Math.floor(time / 60);
                let sec = time % 60;
                $("#timer").text((min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec));
                if (time-- <= 0) {
                    clearInterval(timerInterval);
                    $("#timer").text("시간초과");
                    $("#auth_code").prop("disabled", true);
                }
            }, 1000);
        }

        // 6. 폼 전송 최종 검증 (가입 / 수정 공통)
        $("#joinForm").on("submit", function(e) {
            // 수정 모드일 경우 제출 직전 플래그 최종 동기화
            if($("#user_id").prop("readonly")) {
                isIdChecked = true; 
                if($("#user_pw").val() === "" && $("#user_pw_confirm").val() === "") isPwMatched = true;
                if($("#user_email").val() === initialEmail) isEmailChecked = true;
            }

            if(!isIdChecked) { 
                alert("아이디 중복확인이 필요합니다."); 
                $("#user_id").focus();
                e.preventDefault(); return false; 
            }
            if(!isPwMatched) { 
                alert("비밀번호 일치 여부를 확인해주세요."); 
                $("#user_pw").focus();
                e.preventDefault(); return false; 
            }
            if(!isEmailChecked) { 
                alert("이메일 인증을 완료해주세요."); 
                $("#user_email").focus();
                e.preventDefault(); return false; 
            }
            
            return true; 
        });

        // 7. 점주 가입 2단계 최종 검증
        $("#ownerStep2Form").on("submit", function(e) {
            const lat = $("#store_lat").val();
            const lon = $("#store_lon").val();

            // 주소 검색을 통한 좌표 설정 여부 확인
            if(!lat || lat === "0.0" || !lon || lon === "0.0") {
                alert("주소 검색을 통해 가게 위치(좌표)를 확정해주세요.");
                e.preventDefault();
                return false;
            }
            return true;
        });
    });

    // [C] 회원 탈퇴 함수 (전역 노출 - JSP onclick 대응)
    window.dropUser = function(userId) {
        if (confirm("정말로 탈퇴하시겠습니까?\n모든 예약 및 웨이팅 데이터가 소멸됩니다.")) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = (typeof APP_CONFIG !== 'undefined' ? APP_CONFIG.contextPath : "") + '/member/withdraw';
            
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = APP_CONFIG.csrfName;
            csrfInput.value = APP_CONFIG.csrfToken;
            
            form.appendChild(csrfInput);
            document.body.appendChild(form);
            form.submit();
        }
    };

})(jQuery);