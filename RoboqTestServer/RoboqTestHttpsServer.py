
from __future__ import unicode_literals
from flask import Flask
from OpenSSL import SSL


app = Flask(__name__, static_folder='static')
app.debug = True

@app.route('/test_ssl')
def test_ssl():
    return 'SSL hello'

if __name__ == '__main__':
    context = SSL.Context(SSL.SSLv23_METHOD)
    context.use_privatekey_file('ssl.key')
    context.use_certificate_file('ssl.crt')
    app.run(host='', port=24444, ssl_context=context)