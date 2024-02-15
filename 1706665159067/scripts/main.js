$(document).ready(function () {
    new Swiper(".offers_slider", {
        slidesPerView: 2.5,
        spaceBetween: 55,
        loop: true,
        pagination: {
            el: ".swiper-pagination",
            clickable: true,
            renderBullet: function (index, className) {
                const prefix = index < 9 ? 0 : '';
                return '<span class="' + className + '">' + prefix + (index + 1) + "</span>";
            },
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        autoplay: {
            delay: 5000,
            disableOnInteraction: false,
        },
        breakpoints: {
            0: {
                slidesPerView: 1.8,
                centeredSlides: true,
                spaceBetween: 25,
                autoplay: false,
                pagination: false,
                navigation: false
            },
            768: {
                slidesPerView: 2.5,
                spaceBetween: 30,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            },
            1024: {
                slidesPerView: 2.5,
                spaceBetween: 55,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            },
            1280: {
                slidesPerView: 2.5,
                spaceBetween: 55,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            }
        },
    });

    new Swiper(".help_slider", {
        slidesPerView: 2.5,
        spaceBetween: 50,
        loop: true,
        pagination: {
            el: ".swiper-pagination",
            clickable: true,
            renderBullet: function (index, className) {
                const prefix = index < 9 ? 0 : '';
                return '<span class="' + className + '">' + prefix + (index + 1) + "</span>";
            },
        },
        autoplay: true,
        breakpoints: {
            0: {
                slidesPerView: 1.5,
                spaceBetween: 25,
                autoplay: false,
                pagination: false,
                navigation: false
            },
            768: {
                slidesPerView: 2.5,
                spaceBetween: 20,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            },
            1024: {
                slidesPerView: 2.5,
                spaceBetween: 30,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            },
            1280: {
                slidesPerView: 2.5,
                spaceBetween: 50,
                autoplay: {
                    delay: 5000,
                    disableOnInteraction: false,
                },
            }
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
    });

    new Swiper(".adv-slider", {
        slidesPerView: 1,
        spaceBetween: 10,
        direction: "vertical",
        loop: true,
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
    });

    new Swiper(".adv-slider-2", {
        slidesPerView: 1.8,
        spaceBetween: 25,
        loop: true,
        centeredSlides: true,
    });

    window.addEventListener('scroll',  () => $('nav').css('background', (pageYOffset <= 50) ? 'none' : '#1c1c1cf2'));

    let page = $('html, body');
    $('a[href*="#"]').click(function () {
        page.animate({
            scrollTop: $($.attr(this, 'href')).offset().top
        }, 400);
        return false;
    });

    const isSuccess = Boolean(localStorage.getItem('policy'));
    if (isSuccess === false) {

        $('#cookie').fadeIn();

        $('#cookie_agree').click(function () {
            $('#cookie').fadeOut();
            $.get('/welcome/acceptpolicy');
            localStorage.setItem('policy', 'true');
            document.body.classList.remove('cookie-agreement');
        })

        document.body.classList.add('cookie-agreement');
    }

    // освежаем токен, если в ответе не 401, то мы авторизованы
    fetch('/auth/token/refresh')
        .then(response =>  response.status === 200 ? location.href = location.origin : '');

    fetch('/back/offerTop/getList')
        .then((response) => response.json())
        .then((data) => updateOffersList(data))
        .catch(response => {
            console.log("Offers loading error:", response);
        });

    function updateOffersList(data) {
        let el = document.getElementById('offers-slider');

        const currencies = {
            'USD': '$',
            'EUR': '€',
        }

        for (let item of data) {
            const getCountryIso = () => item?.offer?.offerGeos[0]?.countryISO ?? ''

            let div = document.createElement('div');
            div.className = "offers_slider-item swiper-slide";
            div.innerHTML = `
                <div>
                    <div class="flag fflag fflag-${getCountryIso()}"></div>
                    <p>${item.offer.name.split(' - ')[0]}</p>
                    <p>${currencies[item.offer.offerLeads[0].currency] ?? currencies['USD']} ${item.offer.offerLeads[0].price}</p>
                </div>
                <div class="ofs-image">
                    <img src="https://leadrock.com/storage/offer/basic/${item.offerID}_${item.offer.imageID}_150x150.png" alt="${item.offer.name}">
                </div>`;


            el.appendChild(div);
        }
    }


    $('.switch-language').on('click', function(e) {
        e.preventDefault();
        const language =  $(this).attr('data-lang');
        setLanguage(language);
    });

    setRegistrationSource(location.search);
    setLanguage(getLanguage());


    function setLanguage(language){
        document.cookie = `language=${language}; path=/` + (window.location.protocol === "https:" ? '; Secure' : '');
        window.localStorage.setItem('lang', language === 'ru' ? 'ru' : 'en');

        doRedirect();
    }

    function getLanguage(){
        let cookieLanguage = getCookiesValue('language');
        // Getting language from client (Web Browser) if cookie is not exists
        return cookieLanguage ?? navigator.language.slice(0, 2);
    }

    function doRedirect(){
        // URLs for redirect
        const pages = {'ru': '', 'en': 'en'}
        const language = window.localStorage.getItem('lang');

        if(document.documentElement.lang !== language){
            window.location.href = location.origin + '/authorization/' + (pages[language] ?? 'en') + (location.search ?? '');
        }
    }

    function setRegistrationSource() {

        if (document.referrer === '') return; // blank referrer

        try {
            let url = new URL(document.referrer);
            if (url.host === window.location.host) {
                return; // same origin
            }
        } catch (ex) { /* ignored */ }

        fetch('/back/user/setRegistrationSource' + location.search, {
            headers: { "Request-Referrer": document.referrer },
        });
    }


    // Cookies ==========================================
    function parseCookies() {
       return document.cookie
            .split('; ')
            .map(item => item.split('='))
            .map(([key, value]) => ({key, value}))
    }

    function getCookiesValue(name) {
        const find = parseCookies().find(({key}) => name === key)
        return find ? find.value : null
    }



    // ================== MANAGER'S INVITE ============
    const INVITE_EXPIRATION_IN_MS = (1000 * 60 * 60 * 24) * 30; /* - 30 days */
    const INVITE_GET_PARAMS_KEY = 'invite-';

    initInvite();
    /**
     * @return {string|null} Inviter ID
     */
    function getInviteIdFromURL() {

        let invite = null;

        const search = window.location.search;

        // find the beginning of the invitation key
        let cursorStart = search.indexOf(INVITE_GET_PARAMS_KEY);
        if (cursorStart !== -1) {

            // set cursor to the end of invitation key
            cursorStart += INVITE_GET_PARAMS_KEY.length;

            // find optional ampersand
            let cursorEnd = search.indexOf('&', cursorStart);
            if (cursorEnd !== -1) {
                invite = search.substring(cursorStart, cursorEnd).trim(); // cut up to ampersand if present
            } else {
                invite = search.substring(cursorStart).trim();
            }
        }

        return invite;
    }

    function initInvite() {
        if (isInviteExpired()) {
            forgetInvite();
        }

        let inviteID = getInviteIdFromURL();
        if (inviteID && inviteID !== '') {
            rememberInvite(inviteID);
        }
    }

    function rememberInvite(inviterID) {
        let prevInvite = getInvite();
        if (prevInvite === null || prevInvite !== inviterID) {
            window.localStorage.setItem('inviteID', inviterID);
            window.localStorage.setItem('inviteDate', Date.now().toString());
        }
    }

    function getInvite() {

        if (isInviteExpired()) {
            forgetInvite();
        }

        let inviteID = window.localStorage.getItem('inviteID');
        if (inviteID) {
            return Number(inviteID);
        }
        return null;
    }

    function hasInvite() {

        if (isInviteExpired()) {
            forgetInvite();
        }
        return window.localStorage.getItem('inviteID') !== null;
    }

    function isInviteExpired() {

        let inviteID   = window.localStorage.getItem('inviteID');
        let inviteDate = window.localStorage.getItem('inviteDate');
        if (inviteID === null || inviteDate === null) {
            return true;
        }

        let endDate = new Date(Number(inviteDate) + INVITE_EXPIRATION_IN_MS);
        return Date.now() >= endDate.getTime();
    }

    function forgetInvite() {
        window.localStorage.removeItem('inviteID');
        window.localStorage.removeItem('inviteDate');
    }
});
