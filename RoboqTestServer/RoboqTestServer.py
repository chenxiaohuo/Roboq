from __future__ import unicode_literals
from flask import Flask, request, jsonify, redirect, url_for
import json


app = Flask(__name__, static_folder='static')
app.debug = True


@app.route('/test_plain_text')
def test_plain_text():
    return 'hello'


@app.route('/test_json_object')
def test_json_object():
    return jsonify({'k1': 'v1'})


@app.route('/test_json_array')
def test_json_array():
    a = [
        {'k1': 'v1a', 'k2': 'v2a'},
        {'k1': 'v1b', 'k2': 'v2b'},
    ]
    return json.dumps(a)


@app.route('/test_options/<pv1>/<pv2>')
def test_options(pv1, pv2):
    return jsonify({
        'pv1': pv1,
        'pv2': pv2,
        'p1': request.args.get('p1', ''),
        'ua': request.headers.get('User-Agent', ''),
        'h1': request.headers.get('X-H1', ''),
        'dynaopt1': request.args.get('dyna-opt', ''),
    })


@app.route('/test_post_body', methods=['POST'])
def test_post_body():
    return jsonify({
        'body': request.data,
        'content-type': request.headers.get('Content-Type', '')
    })


@app.route('/test_multipart_post', methods=['POST'])
def test_multipart_post():
    file1 = request.files['bytes']
    file2 = request.files['is']
    file3 = request.files['file']
    text = request.form['tf']
    return jsonify({
        'bytes': {
            'filename': file1.filename,
            'mimetype': file1.mimetype,
            'content-length': file1.content_length,
        },
        'is': {
            'filename': file2.filename,
            'mimetype': file2.mimetype,
            'content-length': file2.content_length,
        },
        'file': {
            'filename': file3.filename,
            'mimetype': file3.mimetype,
            'content-length': file3.content_length,
        },
        'tf': text
    })

@app.route('/test_form_post', methods=['POST'])
def test_form_post():
    t1 = request.form['f1']
    t2 = request.form['f2']
    return jsonify({'f1':t1, 'f2':t2})

@app.route('/test_body_post', methods=['POST'])
def test_body_post():
    btype = request.args.get('type', 'text')
    if btype == 'text':
        return jsonify({
            'content': request.data,
            'mimetype': request.mimetype,
            'content-length': request.content_length})
    elif btype in ['bytes', 'is', 'file']:
        return jsonify({
            'content': request.data,
            'mimetype': request.mimetype,
            'content-length': request.content_length})
    else:
        return jsonify({})



@app.route('/test_redirect')
def test_redirect():
    return redirect(url_for('test_plain_text'))


if __name__ == '__main__':
    app.run(host='', port=23333)
