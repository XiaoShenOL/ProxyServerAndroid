package net.youmi.android.libs.common.download.ext;

import android.content.Context;

import org.json.JSONObject;

/**
 * @author: CsHeng (csheng1204[at]gmail[dot]com)
 * Date: 14-3-12
 * Time: 上午10:55
 */
public interface IConstructable<T> {

	T newInstanceConstructor(JSONObject jsonObject, Context context);

}