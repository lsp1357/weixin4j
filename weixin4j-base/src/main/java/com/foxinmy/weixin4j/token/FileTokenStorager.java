package com.foxinmy.weixin4j.token;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.foxinmy.weixin4j.model.Token;
import com.foxinmy.weixin4j.util.FileUtil;
import com.foxinmy.weixin4j.xml.XmlStream;

/**
 * 用File形式保存Token信息
 *
 * @className FileTokenStorager
 * @author jinyu(foxinmy@gmail.com)
 * @date 2015年1月9日
 * @since JDK 1.6
 */
public class FileTokenStorager extends TokenStorager {

	private final String cachePath;

	public FileTokenStorager(String cachePath) {
		this.cachePath = cachePath;
	}

	@Override
	public Token lookup(String cacheKey) {
		File token_file = new File(String.format("%s/%s.xml", cachePath,
				cacheKey));
		try {
			if (token_file.exists()) {
				Token token = XmlStream.fromXML(
						new FileInputStream(token_file), Token.class);
				if (token.getCreateTime() < 0) {
					return token;
				}
				if ((token.getCreateTime() + (token.getExpiresIn() * 1000l) - ms()) > System
						.currentTimeMillis()) {
					return token;
				}
			}
			return null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void caching(String cacheKey, Token token) {
		try {
			XmlStream.toXML(
					token,
					new FileOutputStream(new File(String.format("%s/%s.xml",
							cachePath, cacheKey))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Token evict(String cacheKey) {
		Token token = null;
		File token_file = new File(String.format("%s/%s.xml", cachePath,
				cacheKey));
		try {
			if (token_file.exists()) {
				token = XmlStream.fromXML(new FileInputStream(token_file),
						Token.class);
				token_file.delete();
			}
		} catch (IOException e) {
			; // ingore
		}
		return token;
	}

	@Override
	public void clear(final String prefix) {
		File[] files = new File(cachePath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile()
						&& file.getName().startsWith(prefix)
						&& "xml".equals(FileUtil.getFileExtension(file
								.getName()));
			}
		});
		for (File token : files) {
			token.delete();
		}
	}
}
