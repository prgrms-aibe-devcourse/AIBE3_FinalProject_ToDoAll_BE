import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    stages: [
        { duration: '10s', target: 5 },
        { duration: '20s', target: 20 },
        { duration: '30s', target: 20 },
        { duration: '10s', target: 0 },
    ],
};

export default function () {
    const payload = JSON.stringify({
        email: `test${__VU}@jobda.com`,
        password: 'password123!',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(
        `${BASE_URL}/api/auth/token`,
        payload,
        params
    );

    check(res, {
        'login status is 200 or 401': (r) => r.status === 200 || r.status === 401,
    });

    sleep(1);
}