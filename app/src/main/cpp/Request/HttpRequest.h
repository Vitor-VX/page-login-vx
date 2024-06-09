//
// Created by tanzi on 13/12/2019.
//
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <netdb.h>
#include <stdarg.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>

#define BUFSIZE 41000
#define URLSIZE 1024
#define INVALID_SOCKET -1
#define __DEBUG__
#define BUFFER_SIZE 1024
#define HTTP_POST "POST /%s HTTP/1.1\r\nHOST: %s:%d\r\nAccept: */*\r\n"\
    "Content-Type:application/x-www-form-urlencoded\r\nContent-Length: %d\r\n\r\n%s"
#define HTTP_GET "GET /%s HTTP/1.1\r\nHOST: %s:%d\r\nAccept: */*\r\n\r\n"
#ifndef _MY_HTTP_H
#define _MY_HTTP_H

#define MY_HTTP_DEFAULT_PORT 80

class HttpRequest
{
public:
    HttpRequest();
    ~HttpRequest();
    void DebugOut(const char *fmt, ...);

    int HttpGet(const char* strUrl, char* strResponse);
    int HttpPost(const char* strUrl, const char* strData, char* strResponse);
    char* GetHostAddrFromUrl(const char* strUrl);
    char* GetIPFromUrl(const char* strUrl);
    char * http_post(const char *url,const char * post_str);
    char * http_get(const char *url);


private:
    const char * token;
    int   HttpRequestExec(const char* strMethod, const char* strUrl, const char* strData, char* strResponse);
    char* HttpHeadCreate(const char* strMethod, const char* strUrl, const char* strData);
    char* HttpDataTransmit(char *strHttpHead, const int iSockFd);

    int   GetPortFromUrl(const char* strUrl);
    char* GetParamFromUrl(const char* strUrl);
    int   SocketFdCheck(const int iSockFd);

    static int m_iSocketFd;
};

#endif

