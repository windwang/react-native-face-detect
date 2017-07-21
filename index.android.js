import React from 'react'
import PropTypes from 'prop-types';

import {requireNativeComponent, View} from 'react-native';

const RCTFaceTrackerView = requireNativeComponent('RCTFaceTrackerView', FaceTrackerView, {
  nativeOnly: {
    onChange: true
  }
});

export default class FaceTrackerView extends React.Component {

  static propTypes = {
    /**
     * 脸部检测事件
     */
    onFaceDetection: PropTypes.func,
    /**
     * 检测脸部图片的宽度，值越小检测速度越快，相应的要求用户距离摄像头越近，默认值是320
     */
    imageWidth: PropTypes.number,
    /**
     * 检测脸部图片的高度，值越小检测速度越快，相应的要求用户距离摄像头越近，默认值是240
     */
    imageHeight: PropTypes.number,

    /**
     * 至少检测到的次数，大于等于此值的脸才会引发检测事件,默认值是3
     */
    minDetectedTimes: PropTypes.number,
    /**
     * 超时时间，超过此时间后用户再出现默认为新用户，检测次数从0开始增加,默认为10秒
     */
    minKeepTime: PropTypes.number,

    /**
     * 最小置信度，大于该值的才被识别为人脸,取值范围为0-1
     */
    confidence:PropTypes.confidence
  }

  constructor(props) {
    super(props);
    this._onChange = this
      ._onChange
      .bind(this)
  }
  _onChange(event) {
    if (!this.props.onFaceDetection) {
      return
    }
    this
      .props
      .onFaceDetection(event.nativeEvent)
  }

  render() {
    return <RCTFaceTrackerView {...this.props} onChange={this._onChange}/>
  }
}
